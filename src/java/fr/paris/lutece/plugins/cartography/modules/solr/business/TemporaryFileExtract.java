package fr.paris.lutece.plugins.cartography.modules.solr.business;

import java.io.IOException;

import fr.paris.lutece.plugins.filegenerator.service.IFileGenerator;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TemporaryFileExtract implements IFileGenerator {
	
	private static final String CONSTANT_MIME_TYPE_JSON = "application/json";
	protected static final String TMP_DIR = System.getProperty( "java.io.tmpdir" );
	private static final int FLUSH_SIZE = 1000;
	public static final String UTF8_BOM = "\uFEFF";
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	private String content = "content";
	
	@Override
	public Path generateFile() throws IOException {
		Path extractFile = Paths.get( TMP_DIR, "ExtractCartoLayer.json" );
	    //writeExportFile( csvFile );
	    return extractFile;
	}
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Extraction Layer";
	}
	
	@Override
	public String getFileName() {
		return "ExtractCartoLayer.json";
	}
	
	@Override
	public String getMimeType() {
		return CONSTANT_MIME_TYPE_JSON;
	}
	
	@Override
	public boolean isZippable() {
		return false;
	}

}
