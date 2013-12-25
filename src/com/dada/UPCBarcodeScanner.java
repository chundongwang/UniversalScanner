package com.dada;

public class UPCBarcodeScanner implements BarcodeScanner {

	private static int COLOR_TOLERANCE = 2;
	private static int UPC_A_TOTAL_MODULES_COUNT = 95;
	private static int UPC_A_LEFT_DIGIT_COUNT = 6;
	private static int UPC_A_RIGHT_DIGIT_COUNT = 6;
	private static int UPC_A_DIGIT_MODULE_COUNT = 7;
	private static int UPC_A_STARTEND_MODULE_COUNT = 3;
	private static int UPC_A_MIDDLE_MODULE_COUNT = 5;

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

	UPCBarcodeScanner(byte[] buffer, int width, int height,
			int bytePerPixel) {
		mBuffer = buffer;
		mWidth = width;
		mHeight = height;
		mBytePerPixel = bytePerPixel;
		mBaseOffset = width * (height + 1) / 2;
		mMaxBarWidth = width / UPC_A_TOTAL_MODULES_COUNT;
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

	private boolean ReadStartEnd() {
		return true;
	}

	private int ReadNumber(int iOffset) throws Exception {
		int value = 0;
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
		return value;
	}

	private boolean ReadMiddle() {
		return true;
	}

	@Override
	public String Scan(byte[] buffer, int width, int height, int bytePerPixel) {
		String result = null;
		if (buffer.length == width * height * bytePerPixel) {
			if (verifyFormat()) {
				int iStartPosition = mStartPosition
						+ UPC_A_STARTEND_MODULE_COUNT * mColumnWidth;
				for (int i = 0; i < UPC_A_LEFT_DIGIT_COUNT; i++) {
					try {
						int binary = ReadNumber(iStartPosition + i * UPC_A_DIGIT_MODULE_COUNT * mColumnWidth);
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
