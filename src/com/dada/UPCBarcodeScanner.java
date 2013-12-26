package com.dada;

public class UPCBarcodeScanner implements BarcodeScanner {

	private static int COLOR_TOLERANCE = 2;
	private static int UPC_A_TOTAL_MODULES_COUNT = 95;
	private static int UPC_A_LEFT_DIGIT_COUNT = 6;
	private static int UPC_A_RIGHT_DIGIT_COUNT = 6;
	private static int UPC_A_DIGIT_MODULE_COUNT = 7;
	private static int UPC_A_STARTEND_MODULE_COUNT = 3;
	private static int UPC_A_MIDDLE_MODULE_COUNT = 5;

	private boolean mInitialized;

	private byte[] mBuffer;
	private int mWidth;
	private int mHeight;
	private int mBytePerPixel;
	private int mBaseOffset;
	private int mMaxBarWidth;

	private int mStartPosition;
	private int mColumnWidth;
	private int mColorBar;
	private int mColorSpace;

	public UPCBarcodeScanner() {
		mInitialized = false;
	}

	private UPCBarcodeScanner(byte[] buffer, int width, int height,
			int bytePerPixel) {
		initialize(buffer, width, height, bytePerPixel);
	}

	private void initialize(byte[] buffer, int width, int height,
			int bytePerPixel) {
		mBuffer = buffer;
		mWidth = width;
		mHeight = height;
		mBytePerPixel = bytePerPixel;
		mBaseOffset = width * (height + 1) / 2;
		mMaxBarWidth = width / UPC_A_TOTAL_MODULES_COUNT;
		mStartPosition = 0;
		mColumnWidth = 0;
		mColorBar = 0;
		mColorSpace = 0;
		mInitialized = true;
	}

	private boolean isSimilarColor(int y1, int y2) {
		return Math.abs(y1 - y2) <= COLOR_TOLERANCE;
	}

	/*
	 * Verify if the column contains the expected color. For now, we'll be only
	 * testing the central spot.
	 */
	private boolean verifySpot(int yExpectedColor, int iCentralSpotToVerify,
			int speculativeBarWidth) {
		int yToVerify = getColorAt(iCentralSpotToVerify);
		return isSimilarColor(yToVerify, yExpectedColor);
	}

	/*
	 * We assume the format of image in buffer is NV21 (YUV420). Since we only
	 * needs brightness info, we'll be picking first section, Y' part. This
	 * function not only verify if the format is matched, but also record the
	 * color for bar/space and width of single column.
	 */
	private boolean verifyFormat() {
		int yLast = getColorAt(0);
		int i = 1;
		boolean formatVerified = false;
		while (i < mWidth) {
			// bar
			int y = getColorAt(i);
			int start = i;
			while (isSimilarColor(y, yLast) && i < mWidth
					&& i - start <= mMaxBarWidth) {
				i++;
				y = getColorAt(i);
			}
			int speculativeBarWidth = i - start;
			if (speculativeBarWidth <= 2
					|| speculativeBarWidth * UPC_A_TOTAL_MODULES_COUNT > mWidth) {
				// Here i is at the start of next color chunk
				continue;
			}

			// bar & space
			// We move iVerify instead of i so that we could just continue to
			// keep searching.
			int iVerify = i + speculativeBarWidth / 2 - 1; // space
			int yBar = yLast;
			int ySpace = getColorAt(iVerify);
			if (!isSimilarColor(yBar, ySpace)) {
				// s 0,1,2,3,4,5 m 0,1,2,3,4,5 e
				// & bar, & space & bar & space & bar & space, & bar & space &
				// bar
				int k;
				for (k = 0; k < 9; k++) {
					iVerify += speculativeBarWidth;
					if (!verifySpot((k % 2 == 0 ? ySpace : yBar), iVerify,
							speculativeBarWidth)) {
						break;
					}

					if (k == 0 || k == 5) {
						boolean verifyThroughDigits = true;
						for (int iDigit = 0; iDigit < UPC_A_LEFT_DIGIT_COUNT; iDigit++) {
							for (int iModule = 0; iModule < UPC_A_DIGIT_MODULE_COUNT; iModule++) {
								iVerify += speculativeBarWidth;
								if (!verifySpot(yBar, iVerify,
										speculativeBarWidth)
										&& !verifySpot(ySpace, iVerify,
												speculativeBarWidth)) {
									verifyThroughDigits = false;
								}
							}
						}
						if (!verifyThroughDigits) {
							// digit not valid
							break;
						}
					}
				}

				if (k == 9) {
					formatVerified = true;
					mColumnWidth = speculativeBarWidth;
					mColorBar = yBar;
					mColorSpace = ySpace;
					mStartPosition = i - speculativeBarWidth / 2 - 1;
					break;
				}
			}
		}
		return formatVerified;
	}

	private int getColorAt(int i) {
		return mBuffer[mBaseOffset + i] & 0xff;
	}

	private int ReadNumber(int iOffset, boolean isLeft) throws Exception {
		int value = 0;
		int result = -1;
		/*
		 * Left: 0 - 0001101,0x0D 1 - 0011001,0x19 2 - 0010011,0x13 3 -
		 * 0111101,0x3D 4 - 0100011,0x23 5 - 0110001,0x31 6 - 0101111,0x2F 7 -
		 * 0111011,0x3B 8 - 0110111,0x37 9 - 0001011,0x0B Right: 0 -
		 * 1110010,0x72 1 - 1100110,0x66 2 - 1101100,0x6C 3 - 1000010,0x42 4 -
		 * 1011100,0x5C 5 - 1001110,0x4E 6 - 1010000,0x50 7 - 1000100,0x44 8 -
		 * 1001000,0x48 9 - 1110100,0x74
		 */
		for (int i = 0; i < UPC_A_DIGIT_MODULE_COUNT; i++) {
			int yColor = getColorAt(iOffset);
			if (isSimilarColor(yColor, mColorBar)) {
				value |= 1 << i;
			} else {
				if (!isSimilarColor(yColor, mColorSpace)) {
					throw new Exception("Not expected color");
				}
			}
		}
		if (isLeft) {
			switch (value) {
			case 0x0D:
				result = 0;
				break;
			case 0x19:
				result = 1;
				break;
			case 0x13:
				result = 2;
				break;
			case 0x3D:
				result = 3;
				break;
			case 0x23:
				result = 4;
				break;
			case 0x31:
				result = 5;
				break;
			case 0x2F:
				result = 6;
				break;
			case 0x3B:
				result = 7;
				break;
			case 0x37:
				result = 8;
				break;
			case 0x0B:
				result = 9;
				break;
			}
		} else {
			switch (value) {
			case 0x72:
				result = 0;
				break;
			case 0x66:
				result = 1;
				break;
			case 0x6C:
				result = 2;
				break;
			case 0x42:
				result = 3;
				break;
			case 0x5C:
				result = 4;
				break;
			case 0x4E:
				result = 5;
				break;
			case 0x50:
				result = 6;
				break;
			case 0x44:
				result = 7;
				break;
			case 0x48:
				result = 8;
				break;
			case 0x74:
				result = 9;
				break;
			}
		}

		if (result < 0 || result > 9) {
			throw new Exception("Invalid number format");
		}

		return result;
	}

	@Override
	public String Scan(byte[] buffer, int width, int height, int bytePerPixel) {
		String result = null;
		initialize(buffer, width, height, bytePerPixel);
		if (buffer.length == width * height * bytePerPixel) {
			if (verifyFormat()) {
				int iStartPosition = mStartPosition
						+ UPC_A_STARTEND_MODULE_COUNT * mColumnWidth;
				for (int i = 0; i < UPC_A_LEFT_DIGIT_COUNT; i++) {
					try {
						int binary = ReadNumber(iStartPosition + i
								* UPC_A_DIGIT_MODULE_COUNT * mColumnWidth, true /* Left */);
						result+=binary;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
				}
				iStartPosition += UPC_A_DIGIT_MODULE_COUNT * UPC_A_LEFT_DIGIT_COUNT * mColumnWidth;
				iStartPosition += UPC_A_MIDDLE_MODULE_COUNT * mColumnWidth;
				result+=" ";
				for (int i = 0; i < UPC_A_RIGHT_DIGIT_COUNT; i++) {
					try {
						int binary = ReadNumber(iStartPosition + i
								* UPC_A_DIGIT_MODULE_COUNT * mColumnWidth, false /* Right */);
						result+=binary;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
				}
			}
			/*
			 * int base = width * (height+1)/2; int i =
			 * scanner.AdvanceToStart(base); while (i<width) {
			 * buffer[base+i]&0xff; }
			 */
		}
		return result;
	}
}
