package it.desimone.risiko.torneo.panels;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExcelFileFilter extends FileFilter {

	@Override
	public boolean accept(File file) {
		boolean result = false;
		if (file != null && file.isDirectory()){
			result = true;
		}
		if (file != null && file.isFile()){
			String fileName = file.getName().toLowerCase();
			if (fileName.endsWith("xls") || fileName.endsWith("xlsx")){
				result = true;
			}
		}
		return result;
	}

	@Override
	public String getDescription() {
		return "xls,xlsx";
	}

}
