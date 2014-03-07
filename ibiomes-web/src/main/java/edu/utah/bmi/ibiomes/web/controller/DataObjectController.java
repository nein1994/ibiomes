package edu.utah.bmi.ibiomes.web.controller;

import java.io.File;

import javax.servlet.http.*;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import edu.utah.bmi.ibiomes.parse.LocalFile;
import edu.utah.bmi.ibiomes.pub.IBIOMESCollection;
import edu.utah.bmi.ibiomes.pub.IBIOMESExperimentAO;
import edu.utah.bmi.ibiomes.pub.IBIOMESFile;
import edu.utah.bmi.ibiomes.pub.IBIOMESFileAO;
import edu.utah.bmi.ibiomes.metadata.IBIOMESFileGroup;
import edu.utah.bmi.ibiomes.web.Utils;

/**
 * View iRODS data object (file)
 * @author Julien Thibault
 *
 */
public class DataObjectController extends AbstractController {

	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		try{
			HttpSession session = request.getSession(true);
			
			String uri = request.getParameter("uri");
			String id = request.getParameter("id");
			int fileId = -1;
			
			if (uri!=null && uri.length()>0){
				if (uri.endsWith("/"))
					uri = uri.substring(0, uri.length()-1);
			}
			else {
				uri = null;
				if (id!=null && id.length()>0){
					fileId = Integer.parseInt(id);
				}
			}
			
			//check authentication
			IRODSAccount irodsAccount = (IRODSAccount)session.getAttribute("SPRING_SECURITY_CONTEXT");
			if (irodsAccount != null)
			{
				String message = (String)request.getParameter("message");
				
				String target = "/file.jsp";
				ModelAndView mav = new ModelAndView(target);
				
				IBIOMESFileAO ibiomesFileAO = new IBIOMESFileAO(irodsAccessObjectFactory, irodsAccount);
				IBIOMESExperimentAO ibiomesExpAO = new IBIOMESExperimentAO(irodsAccessObjectFactory, irodsAccount);
				
				IBIOMESFile file = null;
				if (uri!=null)
					file = ibiomesFileAO.getFileByPath(uri);
				else {
					file = ibiomesFileAO.getFileByID(fileId);
				}
				
				boolean canRead = ibiomesFileAO.isReadable(uri);
				boolean canWrite = ibiomesFileAO.isWritable(uri);
				
				//if CSV file, get data description
				if (file.getFormat().equals(LocalFile.FORMAT_CSV)){
					mav.addObject("csvAxis", file.getMetadata().getValue(edu.utah.bmi.ibiomes.parse.CSVFile.DATA_LABELS));
					mav.addObject("csvUnits", file.getMetadata().getValue(edu.utah.bmi.ibiomes.parse.CSVFile.DATA_UNITS));
				}
	         	
				//get root experiment
				try{
					IBIOMESCollection experiment = ibiomesExpAO.getRootExperiment(uri);
					if (experiment != null){
						mav.addObject("experiment", experiment);
					}
				} catch (FileNotFoundException e){
					//TO DO modify getRootExperiment to return null
				}
				
				//return collection path as HTML navigation links
				String htmlNav = null;
				if (irodsAccount.isAnonymousAccount())
					htmlNav  = Utils.getNavigationLinkDiabled(uri);
				else
					htmlNav  = Utils.getNavigationLink(uri);
	
				//get copy of the file for Jmol rendering
				if (IBIOMESFileGroup.isJmolFile(file.getFormat()) || IBIOMESFileGroup.isImageFile(file.getFormat()))
				{
					IRODSFileFactory iFileFactory = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount);
					IRODSFile iFile = iFileFactory.instanceIRODSFile(uri);
					
					String relativePath = session.getAttribute("USER_DIR") + "/" + iFile.getAbsolutePath().replaceAll("/", "_");
					String localFilePath = getServletContext().getRealPath("/") + "/" + relativePath;
					File localFile = new File(localFilePath);
					
					String serverUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
					String fileUrl = serverUrl + request.getContextPath() + "/" + relativePath;
					
					//check if the file has already been copied
					if (!localFile.exists())
				    {
						//if user has read access to the file
						if (iFile.canRead()){
							DataTransferOperations dataTransfer = irodsAccessObjectFactory.getDataTransferOperations(irodsAccount);
							dataTransfer.getOperation(iFile, localFile, null, null);
						}
					}
					mav.addObject("localFilePath", localFile.getAbsolutePath());
					mav.addObject("localFileUrl", fileUrl);
					mav.addObject("relativePath", relativePath);
				}
				
				mav.addObject("navLink", htmlNav);
				mav.addObject("zone", irodsAccount.getZone());
				mav.addObject("file", file);
				mav.addObject("fileFormat", file.getFormat());
				mav.addObject("fileMedia", file.getType());
				mav.addObject("fileDescription", file.getDescription());
	
				mav.addObject("canRead", canRead);
				mav.addObject("canWrite", canWrite);
				
				if (message!=null && message.length()>0)
					mav.addObject("message", message);
				
				return mav;
			}
			else 
			{
				ModelAndView mav = new ModelAndView("index.do");
				return mav;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			ModelAndView mav = new ModelAndView("/error.do");
			mav.addObject("exception", e);
			return mav;
		}
	}
	 
	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	public void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}
}
