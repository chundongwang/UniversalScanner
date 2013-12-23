package com.dada;

public interface BarcodeScanner {
	public String Scan(byte[] buffer, int width, int length, int bytePerPixel);
}
