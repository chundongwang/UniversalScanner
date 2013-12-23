package com.dada;

public class UPCBarcodeScanner implements BarcodeScanner {

	@Override
	public String Scan(byte[] buffer, int width, int height, int bytePerPixel) {
		String result = null;
		if (buffer.length == width * height * bytePerPixel) {
			int base = width * (height+1)/2;
			for (int i = 0 ; i < width ; i++) {
				//buffer[base+i]
			}
		}
		return result;
	}

}
