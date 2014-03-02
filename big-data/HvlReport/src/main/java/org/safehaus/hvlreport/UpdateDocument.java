package org.safehaus.hvlreport;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.Table;
import org.apache.poi.hslf.model.TableCell;
import org.apache.poi.hslf.model.TextBox;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Pair {
	String text;
	Date date;

	public Pair(String text, Date date) {
		this.text = text;
		this.date = date;
	}

	public Pair(String text) {
		this.text = text;
		this.date = null;
	}

}

public class UpdateDocument {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	String templateName = "./target/classes/Template.ppt";
	String targetName = "./target/classes/Result.ppt";

	public boolean UpdateTables(Date reportDate, List<Pair> completedTasks, List<Pair> ongoingTasks, List<Pair> plannedTasks, List<Pair> problems) {
		FileInputStream fileInputStream = null;
		SlideShow slideShow;
		try {
			fileInputStream = new FileInputStream(templateName);
			slideShow = new SlideShow(fileInputStream);
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			logger.error("Can't find file {}", templateName, e);
			return false;
		} catch (IOException e) {
			logger.error("Error reading file {}", templateName, e);
			if (null != fileInputStream) {
				try {
					fileInputStream.close();
				} catch (IOException e1) {
					logger.error("Can't close file {}", templateName, e);
				}
			}
			return false;
		}

		Slide[] slides = slideShow.getSlides();
		logger.debug("Number of slides: {}", slides.length);
		if (slides.length < 1) {
			logger.error("Template file {} does not contain a page", templateName);
			return false;
		}

		Slide firstSlide = slides[0];
		
		TextRun[] textRuns = firstSlide.getTextRuns();
		logger.debug("Number of text runs: {}", textRuns.length);
		for (TextRun textRun : textRuns) {
			if (textRun.getText().contains("<DATE>"))
			{
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
				textRun.setText(textRun.getText().replaceAll("<DATE>", sdf.format(reportDate)));
			}
		}
		Shape[] shapes = firstSlide.getShapes();
		logger.debug("Number of shapes: {}", shapes.length);

		int tableCount = 0;
		for (Shape shape : shapes) {
			if (Table.class.isInstance(shape)) {
				Table table = (Table) shape;
				tableCount++;
				switch (tableCount) {
				case 1:
					createTable(completedTasks, table, firstSlide);
					break;
				case 2:
					createTable(ongoingTasks, table, firstSlide);
					break;
				case 3:
					createTable(plannedTasks, table, firstSlide);
					break;
				case 4:
					createTable(problems, table, firstSlide);
					break;
				default:
					logger.error("Template file {} contains more than 4 tables", templateName);
					return false;
				}
				logger.debug("Shape type: {}, name: {}, id: {}, class: {}",shape.getShapeType(), shape.getShapeName(), shape.getShapeId(), shape.getClass());
			}
		}
		if (0 == tableCount)
		{
			logger.error("Template file {} contains no tables", templateName);
			return false;
		}

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(targetName);
			slideShow.write(fileOutputStream);
			fileOutputStream.close();
		} catch (IOException e) {
			logger.error("Can't write file {}", targetName, e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Table createTable(List<Pair> information, Table table, Slide firstSlide)
	{
		if (information.size() == 0)
			return table;
        Table newTable;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        if (null == information.get(0).date) {
        	newTable = new Table(information.size(), 1);
        }
        else
        {
        	newTable = new Table(information.size(), 2);
        }
        
        for (int row = 0; row < information.size(); row++) {
            TableCell cell = newTable.getCell(row, 0);
            cell.setText(information.get(row).text);
            RichTextRun rt = cell.getTextRun().getRichTextRuns()[0];
            rt.setFontName("Arial");
            rt.setFontSize(14);
            rt.setFontColor(Color.black);
            cell.setVerticalAlignment(TextBox.AnchorMiddle);
            cell.setHorizontalAlignment(TextBox.AlignLeft);
            
            if (null != information.get(0).date) {        
	            cell = newTable.getCell(row, 1);
	            cell.setText(sdf.format(information.get(row).date));
	            rt = cell.getTextRun().getRichTextRuns()[0];
	            rt.setFontName("Arial");
	            rt.setFontSize(14);
	            rt.setFontColor(Color.black);
	            cell.setVerticalAlignment(TextBox.AnchorMiddle);
	            cell.setHorizontalAlignment(TextBox.AlignLeft);
            }
        }

        newTable.setColumnWidth(0, 290);

		firstSlide.addShape(newTable);
		Rectangle anchor = table.getAnchor();
		newTable.setAnchor(anchor);
		firstSlide.removeShape(table);

        return newTable;

	}

}