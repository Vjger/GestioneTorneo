package it.desimone.risiko.torneo.batch;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelUtils {

	
	private static void deleteColumn(Sheet sheet, int columnToDelete) {
		int maxColumn = 0;
		for (int r = 0; r < sheet.getLastRowNum() + 1; r++) {
			Row row = sheet.getRow(r);

			// if no row exists here; then nothing to do; next!
			if (row == null)
				continue;

			// if the row doesn't have this many columns then we are good; next!
			int lastColumn = row.getLastCellNum();
			if (lastColumn > maxColumn)
				maxColumn = lastColumn;

			if (lastColumn < columnToDelete)
				continue;

			for (int x = columnToDelete + 1; x < lastColumn + 1; x++) {
				Cell oldCell = row.getCell(x - 1);
				if (oldCell != null)
					row.removeCell(oldCell);

				Cell nextCell = row.getCell(x);
				if (nextCell != null) {
					//Cell newCell = row.createCell(x - 1, nextCell.getCellType());
					Cell newCell = row.createCell(x - 1, nextCell.getCellTypeEnum());
					cloneCell(newCell, nextCell);
				}
			}
		}
	}

	private static void cloneCell(Cell cNew, Cell cOld) {
		cNew.setCellComment(cOld.getCellComment());
		cNew.setCellStyle(cOld.getCellStyle());

		switch (cNew.getCellTypeEnum()) {
		case BOOLEAN: {
			cNew.setCellValue(cOld.getBooleanCellValue());
			break;
		}
		case NUMERIC:
		case BLANK:{ //Pezza perchè la libreria restituisce BLANK come tipo di una cella numerica ancora vuota 
			cNew.setCellValue(cOld.getNumericCellValue());
			break;
		}
		case STRING: {
			cNew.setCellValue(cOld.getStringCellValue());
			break;
		}
		case ERROR: {
			cNew.setCellValue(cOld.getErrorCellValue());
			break;
		}
		case FORMULA: {
			cNew.setCellFormula(cOld.getCellFormula());
			break;
		}
		}
	}
	
	private static void cloneCellOld(Cell cNew, Cell cOld) {
		cNew.setCellComment(cOld.getCellComment());
		cNew.setCellStyle(cOld.getCellStyle());

		switch (cNew.getCellType()) {
		case Cell.CELL_TYPE_BOOLEAN: {
			cNew.setCellValue(cOld.getBooleanCellValue());
			break;
		}
		case Cell.CELL_TYPE_NUMERIC: {
			cNew.setCellValue(cOld.getNumericCellValue());
			break;
		}
		case Cell.CELL_TYPE_STRING: {
			cNew.setCellValue(cOld.getStringCellValue());
			break;
		}
		case Cell.CELL_TYPE_ERROR: {
			cNew.setCellValue(cOld.getErrorCellValue());
			break;
		}
		case Cell.CELL_TYPE_FORMULA: {
			cNew.setCellFormula(cOld.getCellFormula());
			break;
		}
		}
	}

	public static void keepOnlyColumnsWithHeaders(Sheet sheet, List<String> columnHeaders) {
		Row row = sheet.getRow(0);
		if (row == null) {
			return;
		}

		int lastColumn = row.getLastCellNum();

		for (int x = lastColumn; x >= 0; x--) {
			Cell headerCell = row.getCell(x);
			if (headerCell != null && headerCell.getStringCellValue() != null
					&& !columnHeaders.contains(headerCell.getStringCellValue())) {
				deleteColumn(sheet, x);
			}
		}
	}

	public void deleteColumnsWithHeader(Sheet sheet, String columnHeader) {
		Row row = sheet.getRow(0);
		if (row == null) {
			return;
		}

		int lastColumn = row.getLastCellNum();

		for (int x = lastColumn; x >= 0; x--) {
			Cell headerCell = row.getCell(x);
			if (headerCell != null
					&& headerCell.getStringCellValue() != null
					&& headerCell.getStringCellValue().equalsIgnoreCase(
							columnHeader)) {
				deleteColumn(sheet, x);
			}
		}
	}
	
}
