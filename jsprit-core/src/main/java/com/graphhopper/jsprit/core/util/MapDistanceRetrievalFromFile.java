package com.graphhopper.jsprit.core.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapDistanceRetrievalFromFile {

	private static Properties prop;
	private static InputStream input;
	private final static Logger logger = LoggerFactory.getLogger(MapDistanceRetrievalFromFile.class);
	
	static{
		prop = new Properties();
		input = null;
		try {
			input = new FileInputStream("input/map/distances.properties");
			prop.load(input);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//using a property file to store a distance matrix
	public static double getDistanceFromFile(Coordinate coord1, Coordinate coord2){
		String searchTarget = coord1.getX()+"_"+coord1.getY()+"-"+coord2.getX()+"_"+coord2.getY();
		//logger.info("searching for |{}| ", searchTarget);
		return Double.parseDouble(prop.getProperty(searchTarget));
	}
	
	public void closeInput(){
		if(input != null){
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
