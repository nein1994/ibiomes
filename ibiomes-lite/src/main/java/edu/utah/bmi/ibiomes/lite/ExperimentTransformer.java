package edu.utah.bmi.ibiomes.lite;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import edu.utah.bmi.ibiomes.catalog.MetadataLookup;
import edu.utah.bmi.ibiomes.conf.IBIOMESConfiguration;
import edu.utah.bmi.ibiomes.experiment.comp.ParameterSet;
import edu.utah.bmi.ibiomes.experiment.summary.SummaryExperimentTasks;
import edu.utah.bmi.ibiomes.experiment.summary.SummaryMolecularSystems;
import edu.utah.bmi.ibiomes.graphics.plot.PlotGenerator;
import edu.utah.bmi.ibiomes.metadata.MetadataAVUList;
import edu.utah.bmi.ibiomes.metadata.MetadataAttribute;
import edu.utah.bmi.ibiomes.metadata.MethodMetadata;
import edu.utah.bmi.ibiomes.metadata.TopologyMetadata;
import edu.utah.bmi.ibiomes.parse.LocalFile;
import edu.utah.bmi.ibiomes.parse.chem.ExperimentFolder;

/**
 * Utility class to store experiment summaries in various formats
 * @author Julien Thibault, University of Utah
 *
 */
public class ExperimentTransformer {

	public static BaseColor GREEN = new BaseColor(0.0f, 102.0f/255.0f, 0.0f);
	public static Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 24, Font.BOLD, GREEN);
	public static Font abstractFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.ITALIC, BaseColor.DARK_GRAY);
	public static Font subtitleFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD, GREEN);
	
	public static Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
	public static Font boldGreyFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.LIGHT_GRAY);
	public static Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
	public static Font subscriptFont = new Font(Font.FontFamily.TIMES_ROMAN, 7, Font.NORMAL);
	
	public static int N_CELLS_PER_ROW = 2;
	
	
	private MetadataLookup metadataLookupIndex;

	public ExperimentTransformer(MetadataLookup metadataLookupIndex){
		this.metadataLookupIndex = metadataLookupIndex;
	}
	
	/**
	 * Store experiment summary to PDF document
	 * @param outputFilePath Path to PDF file
	 * @throws Exception 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public void storeExperimentAsPDF(ExperimentFolder experiment, String outputFilePath) throws IllegalArgumentException, IllegalAccessException, Exception{
		
		boolean outputToConsole = IBIOMESConfiguration.getInstance().isOutputToConsole();
		
		//TODO Use XSL and FOP (http://xmlgraphics.apache.org/fop/) to generate PDFs from XML
		
		
		String tempDirPath = System.getenv(IBIOMESLiteManager.IBIOMES_HOME_ENV_VAR) + "/" + IBIOMESLiteManager.IBIOMES_TEMP_DIR;

    	if (outputToConsole)
    		System.out.println("Initializing PDF document...");
    	
		//create empty PDF document
		Rectangle page = new Rectangle(PageSize.A4);
		page.setBackgroundColor(BaseColor.WHITE);
		com.itextpdf.text.Document document = new com.itextpdf.text.Document(page);
	    PdfWriter.getInstance(document, new FileOutputStream(outputFilePath));
	    document.open();
	    
	    //add document metadata
	    document.addTitle(experiment.getName());
	    document.addAuthor(System.getProperty("user.name"));
	    document.addCreator("iBIOMES");
	    document.addCreationDate();
	    
    	if (outputToConsole)
    		System.out.println("Adding text...");
    	
	    // Title
	    Paragraph title = new Paragraph();
	    title.add(new Paragraph(experiment.getName().toUpperCase(), titleFont));
	    title.add(new Paragraph("Report automatically generated by iBIOMES Lite (" + System.getProperty("user.name") + ", " + new Date() + ")", boldGreyFont));
	    if (experiment.getDescription()!=null && experiment.getDescription().trim().length()>0){
	    	title.add(new Paragraph(" "));
	    	title.add(new Paragraph(experiment.getDescription(), abstractFont));
	    }
	    title.add(new Paragraph(" "));
	    title.add(new Paragraph(" "));
	    document.add(title);

	    MetadataAVUList metadata = experiment.getFileDirectory().getMetadata();
	    SummaryMolecularSystems systemsSummary = experiment.getMolecularSystemsSummary();
	    SummaryExperimentTasks tasksSummary = experiment.getTasksSummary();
	    HashMap<String, List<LocalFile>> analysisFiles = experiment.getAnalysisFiles();
	    
	    //topology
	    Anchor topologyAnchor = new Anchor("Molecular system", subtitleFont);
	    topologyAnchor.setName("Molecular system");
	    document.add(new Paragraph(topologyAnchor));
	    Paragraph topologyParagraph = new Paragraph();
	    topologyParagraph.add(new LineSeparator(0.5f, 100.0f, BaseColor.LIGHT_GRAY, LineSeparator.ALIGN_BOTTOM, 0.0f));
	    topologyParagraph.add(new Paragraph(" "));
	    
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.MOLECULAR_SYSTEM_DESCRIPTION, metadata.getValues(TopologyMetadata.MOLECULAR_SYSTEM_DESCRIPTION),"");
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.MOLECULE_TYPE, metadata.getValues(TopologyMetadata.MOLECULE_TYPE),"");
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.MOLECULE_DESCRIPTION, metadata.getValues(TopologyMetadata.MOLECULE_DESCRIPTION),"");
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.RESIDUE_CHAIN, metadata.getValues(TopologyMetadata.RESIDUE_CHAIN),"");
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.RESIDUE_CHAIN_NORM, metadata.getValues(TopologyMetadata.RESIDUE_CHAIN_NORM),"");
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.RESIDUE_NON_STD, metadata.getValues(TopologyMetadata.RESIDUE_NON_STD),"");
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.CHEMICAL_FORMULA, metadata.getValues(TopologyMetadata.CHEMICAL_FORMULA),"");
	    List<String> compositions = metadata.getValues(TopologyMetadata.MOLECULE_ATOMIC_COMPOSITION);
	    if (compositions!=null && compositions.size()>0){
	    	topologyParagraph.add(new Phrase("Molecular composition" + ": ", boldFont));
	    	for (String compo : compositions){
	    		String[] elts = compo.split("\\s");
	    		for (int e=0;e<elts.length;e++){
	    			String[] elt = elts[e].split("\\:");
	    			topologyParagraph.add(new Chunk(elt[0], normalFont));
	    			Chunk subScript = new Chunk(elt[1], subscriptFont);
	    		    subScript.setTextRise(-2f);
	    			topologyParagraph.add(subScript);
	    		}
	    		topologyParagraph.add("\n");
	    	}
	    }
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.MOLECULE_ATOMIC_WEIGHT, metadata.getValues(TopologyMetadata.MOLECULE_ATOMIC_WEIGHT),"g/mol");
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.COUNT_ATOMS, metadata.getValues(TopologyMetadata.COUNT_ATOMS),"");
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.COUNT_IONS, metadata.getValues(TopologyMetadata.COUNT_IONS),"");
	    topologyParagraph = addPdfChunkForAVU(topologyParagraph, TopologyMetadata.COUNT_SOLVENT, metadata.getValues(TopologyMetadata.COUNT_SOLVENT),"");

	    document.add(topologyParagraph);
	    document.add(new Paragraph(" "));
	    
	    //method
	    String method = metadata.getValue(MethodMetadata.COMPUTATIONAL_METHOD_NAME);
	    if (method==null || method.trim().length()==0)
	    	method = "Computational method";
	    Anchor methodAnchor = new Anchor(method, subtitleFont);
	    methodAnchor.setName(method);
	    document.add(new Paragraph(methodAnchor));
	    Paragraph methodParagraph = new Paragraph();
	    methodParagraph.add(new LineSeparator(0.5f, 100.0f, BaseColor.LIGHT_GRAY, LineSeparator.ALIGN_BOTTOM, 0.0f));
	    methodParagraph.add(new Paragraph(" "));

	    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.BOUNDARY_CONDITIONS, metadata.getValues(MethodMetadata.BOUNDARY_CONDITIONS),"");
	    String solventType = metadata.getValue(MethodMetadata.SOLVENT_TYPE);
	    if(solventType!=null && solventType.length()>0){
		    if (solventType.equals(ParameterSet.SOLVENT_IMPLICIT) && metadata.containsAttribute(MethodMetadata.IMPLICIT_SOLVENT_MODEL))
		    	solventType = solventType + " (" + metadata.getValue(MethodMetadata.IMPLICIT_SOLVENT_MODEL) + ")";
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.SOLVENT_TYPE, solventType ,"");
	    }
	    if (method.equals(ParameterSet.METHOD_MM) || 
	    		method.equals(ParameterSet.METHOD_MD) || 
	    		method.equals(ParameterSet.METHOD_LANGEVIN_DYNAMICS)|| 
	    		method.equals(ParameterSet.METHOD_QMMM)
	    		)
	    {
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.FORCE_FIELD, metadata.getValues(MethodMetadata.FORCE_FIELD),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.MM_INTEGRATOR, metadata.getValues(MethodMetadata.MM_INTEGRATOR),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.ELECTROSTATICS_MODELING, metadata.getValues(MethodMetadata.ELECTROSTATICS_MODELING),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.UNIT_SHAPE, metadata.getValues(MethodMetadata.UNIT_SHAPE),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.ENSEMBLE_MODELING, metadata.getValues(MethodMetadata.ENSEMBLE_MODELING),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.BAROSTAT_ALGORITHM, metadata.getValues(MethodMetadata.BAROSTAT_ALGORITHM),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.THERMOSTAT_ALGORITHM, metadata.getValues(MethodMetadata.THERMOSTAT_ALGORITHM),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.REFERENCE_TEMPERATURE, metadata.getValues(MethodMetadata.REFERENCE_TEMPERATURE),"K");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.REFERENCE_PRESSURE, metadata.getValues(MethodMetadata.REFERENCE_PRESSURE),"bar");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.CONSTRAINT_ALGORITHM, metadata.getValues(MethodMetadata.CONSTRAINT_ALGORITHM),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.RESTRAINT_TYPE, metadata.getValues(MethodMetadata.RESTRAINT_TYPE),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.LANGEVIN_COLLISION_FREQUENCY, metadata.getValues(MethodMetadata.LANGEVIN_COLLISION_FREQUENCY),"ps-1");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.STOCHASTICS_NOISE_TERM_AMPLITUDE, metadata.getValues(MethodMetadata.STOCHASTICS_NOISE_TERM_AMPLITUDE),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.SIMULATED_TIME, metadata.getValues(MethodMetadata.SIMULATED_TIME),"ns");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.TIME_STEP_LENGTH, metadata.getValues(MethodMetadata.TIME_STEP_LENGTH),"ps");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.ENHANCED_SAMPLING_METHOD_NAME, metadata.getValues(MethodMetadata.ENHANCED_SAMPLING_METHOD_NAME),"p");
		}
	    
	    if (method.equals(ParameterSet.METHOD_QM) || 
	    		method.equals(ParameterSet.METHOD_SEMI_EMPIRICAL) || 
	    		method.equals(ParameterSet.METHOD_QMMM)
	    		)
	    {
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.QM_METHOD_NAME, metadata.getValues(MethodMetadata.QM_METHOD_NAME),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.QM_EXCHANGE_CORRELATION, metadata.getValues(MethodMetadata.QM_EXCHANGE_CORRELATION),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.QM_BASIS_SET, metadata.getValues(MethodMetadata.QM_BASIS_SET),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.QM_SPIN_MULTIPLICITY, metadata.getValues(MethodMetadata.QM_SPIN_MULTIPLICITY),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, TopologyMetadata.TOTAL_MOLECULE_CHARGE, metadata.getValues(TopologyMetadata.TOTAL_MOLECULE_CHARGE),"");
		    methodParagraph = addPdfChunkForAVU(methodParagraph, MethodMetadata.CALCULATION, metadata.getValues(MethodMetadata.CALCULATION),"");
	    }
	    
	    document.add(methodParagraph);
	    document.add(new Paragraph(" "));
	    
	    //analysis data
	    Anchor analysisAnchor = new Anchor("Analysis data", subtitleFont);
	    analysisAnchor.setName("Analysis data");
	    document.add(new Paragraph(analysisAnchor));
	    
	    //retrieve images
	    ArrayList<LocalFile> images = new ArrayList<LocalFile>();
	    
	    HashMap<String, List<LocalFile>> files = experiment.getAnalysisFiles();
	    
	    if (files.containsKey(LocalFile.FORMAT_JPEG))
	    	images.addAll(files.get(LocalFile.FORMAT_JPEG));
	    if (files.containsKey(LocalFile.FORMAT_PNG))
	    	images.addAll(files.get(LocalFile.FORMAT_PNG));
	    if (files.containsKey(LocalFile.FORMAT_BMP))
	    	images.addAll(files.get(LocalFile.FORMAT_BMP));
	    if (files.containsKey(LocalFile.FORMAT_GIF))
	    	images.addAll(files.get(LocalFile.FORMAT_GIF));
	    
	    Paragraph analysisParagraph = new Paragraph();
	    analysisParagraph.setFont(normalFont);
	    analysisParagraph.add(new LineSeparator(0.5f, 100.0f, BaseColor.LIGHT_GRAY, LineSeparator.ALIGN_BOTTOM, 0.0f));
	    analysisParagraph.add(new Paragraph(" "));
	    
	    if (images.size()>0)
	    {
	    	if (outputToConsole)
	    		System.out.println("Adding " + images.size() + " images to the document...");
	    	
		    PdfPTable table = new PdfPTable(N_CELLS_PER_ROW);
		    table.setWidthPercentage(100.0f);
		    		    
		    for (LocalFile imageFile : images){
			    Image image = Image.getInstance(imageFile.getAbsolutePath());
			    PdfPCell imgCell = new PdfPCell(image, true);
			    imgCell.setBorder(PdfPCell.NO_BORDER);
			    imgCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			    imgCell.setPadding(10.0f);
			    table.addCell(imgCell);
		    }
		    //complete row
		    int nCellsLeft = N_CELLS_PER_ROW - (images.size() % N_CELLS_PER_ROW);
		    for (int c=0;c<nCellsLeft;c++){
		    	PdfPCell cell = new PdfPCell();
		    	cell.setBorder(PdfPCell.NO_BORDER);
		    	table.addCell(cell);
		    }
		    analysisParagraph.add(table);
	    }
	    
		//try to generate plots for CSV files
	    if (files.containsKey(LocalFile.FORMAT_CSV)){

	    	PlotGenerator plotTool = new PlotGenerator();
	    	PdfPTable table = new PdfPTable(N_CELLS_PER_ROW);
		    table.setWidthPercentage(100.0f);
	    	List<LocalFile> csvFiles = files.get(LocalFile.FORMAT_CSV);
	    	
	    	if (outputToConsole)
	    		System.out.println("Generating " + csvFiles.size() + " plots from CSV files...");
	    	
	    	//for each CSV file
	    	for (LocalFile csvFile : csvFiles)
	    	{
	    		String dataFilePath = csvFile.getAbsolutePath();
				try{
					String imageFilePath = tempDirPath + "/" + csvFile.getName() + "_plot";
					IBIOMESLiteManager.generatePlotForCSV(
							plotTool, 
							dataFilePath, 
							csvFile.getMetadata(), 
							imageFilePath,
							"png");
					Image image = Image.getInstance(imageFilePath + ".png");
					PdfPCell imgCell = new PdfPCell(image, true);
				    imgCell.setBorder(PdfPCell.NO_BORDER);
				    imgCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
				    imgCell.setPadding(10.0f);
				    table.addCell(imgCell);
					
				} catch (Exception e){
					System.out.println("Warning: Plot for '"+ dataFilePath + "' could not be generated.");
				}
	    	}
	    	 //complete row
		    int nCellsLeft = N_CELLS_PER_ROW - (images.size() % N_CELLS_PER_ROW);
		    for (int c=0;c<nCellsLeft;c++){
		    	PdfPCell cell = new PdfPCell();
		    	cell.setBorder(PdfPCell.NO_BORDER);
		    	table.addCell(cell);
		    }
		    analysisParagraph.add(table);
	    }
	    
	    
	    if (images.size()==0 && 
	    		!files.containsKey(LocalFile.FORMAT_CSV)) {
	    	analysisParagraph.add("No analysis data available");
	    }
	    
	    document.add(analysisParagraph);
	    
	    //close document to save
	    document.close();
	    
	    //clean temp directory
    	File tempDir = new File(tempDirPath);
    	File[] filesToDelete = tempDir.listFiles();
    	for (int f=0;f<filesToDelete.length;f++){
    		filesToDelete[f].delete();
    	}
	}
	
	private Paragraph addPdfChunkForAVU(Paragraph paragraph, String attributeCode, List<String> values, String unit) throws Exception
	{
    	if (values.size() > 0)
    	{
    		//find attribute term
    		MetadataAttribute attribute = metadataLookupIndex.lookupMetadataAttribute(attributeCode);
    		String attributeTerm = attribute.getCode();
        	if (attribute.isStandard())
        		attributeTerm = attribute.getTerm();
        	paragraph.add(new Phrase(attributeTerm + ": ", boldFont));
        	
        	//display values
        	if (values.size() > 1){
        		com.itextpdf.text.List list = new com.itextpdf.text.List();
        		for (String value : values){
        			list.add(new ListItem(value + " " + unit, normalFont));
        		}
        		paragraph.add(list);
        	}
        	else
        		paragraph.add(new Phrase(values.get(0) + " " + unit+ "\n", normalFont));
    	}
    	return paragraph;		
	}
	
	private Paragraph addPdfChunkForAVU(Paragraph paragraph, String attributeCode, String value, String unit) throws Exception
	{
		List<String> values = new ArrayList<String>();
		values.add(value);
    	return addPdfChunkForAVU(paragraph, attributeCode, values, unit);	
	}
}
