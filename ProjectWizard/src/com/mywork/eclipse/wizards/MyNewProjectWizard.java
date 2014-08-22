/**
 * 
 */
package com.mywork.eclipse.wizards;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.mywork.eclipse.base.MyWizardNewProjectCreationPage;

/**
 * Creates a new project containing folders and files for use with an
 * Example.com enterprise web site.
 * 
 * @author Nathan A. Good &lt;mail@nathanagood.com&gt;
 * 
 */
public class MyNewProjectWizard extends Wizard implements INewWizard,
		IExecutableExtension {

	/*
	 * Use the WizardNewProjectCreationPage, which is provided by the Eclipse
	 * framework.
	 */
	private MyWizardNewProjectCreationPage wizardPage;

	private IConfigurationElement config;

	private IWorkbench workbench;

	private IStructuredSelection selection;

	private IProject project;
	
	private  String templatePathString;
	private String  authorString;

	/**
	 * Constructor
	 */
	public MyNewProjectWizard() {
		super();
	}

	public void addPages() {
		/*
		 * Unlike the custom new wizard, we just add the pre-defined one and
		 * don't necessarily define our own.
		 */
		wizardPage = new MyWizardNewProjectCreationPage(
				"NewExampleComSiteProject");
		wizardPage.setDescription("create a new project");
		wizardPage.setTitle("create a new project");
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {

		if (project != null) {
			return true;
		}

		final IProject projectHandle = wizardPage.getProjectHandle();

		URI projectURI = (!wizardPage.useDefaults()) ? wizardPage
				.getLocationURI() : null;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		final IProjectDescription desc = workspace
				.newProjectDescription(projectHandle.getName());

		desc.setLocationURI(projectURI);
		
		templatePathString=wizardPage.getTemplateLocationPath().toString();
		
		authorString=wizardPage.getAuthor();
		
		

		/*
		 * Just like the NewFileWizard, but this time with an operation object
		 * that modifies workspaces.
		 */
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
					throws CoreException {
				createProject(desc, projectHandle, monitor);
			}
		};

		/*
		 * This isn't as robust as the code in the BasicNewProjectResourceWizard
		 * class. Consider beefing this up to improve error handling.
		 */
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException
					.getMessage());
			return false;
		}

		project = projectHandle;

		if (project == null) {
			return false;
		}

		BasicNewProjectResourceWizard.updatePerspective(config);
		BasicNewProjectResourceWizard.selectAndReveal(project, workbench
				.getActiveWorkbenchWindow());

		return true;
	}

	
	//add a new file，set all the variables needed into paramMap
	void addFile(IContainer container, IProgressMonitor monitor,Map<String, Object> paramMap) throws Exception {
		
		InputStream resourceStream=MyVelocityUtil.Parse(paramMap);
		int iRet=addFileToProject(container,new Path((String) paramMap.get("outputFile")),resourceStream,monitor);
		if(iRet==0){
			resourceStream.close();
		}

		return;
	}
	
	/**
	 * This creates the project in the workspace.
	 * 
	 * @param description
	 * @param projectHandle
	 * @param monitor
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	void createProject(IProjectDescription description, IProject proj,
			IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		try {	
			System.out.println(System.getProperty("user.dir"));// user.dir is the current parth
			
			monitor.beginTask("", 2000);

			proj.create(description, new SubProgressMonitor(monitor, 1000));

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			proj.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(
					monitor, 1000));

			/*
			 * Okay, now we have the project and we can do more things with it
			 * before updating the perspective.
			 */
			IContainer container = (IContainer) proj;
			System.out.println("template path:"+templatePathString+" author:"+authorString);
	
			TemplateFileHandle templateHandle=new TemplateFileHandle(templatePathString,proj.getName());
			
			Set<String> MyFolders=templateHandle.getFolderSet();
			Map<String,String> files=templateHandle.getFileMap();
			Set<String> fileKeySets=files.keySet();
								
			Map<String, IFolder> MyFolderMaps = new HashMap<String, IFolder>();
			
			//create folder
			for (String folder : MyFolders) {
				MyFolderMaps.put(folder,
						container.getFolder(new Path(folder)));
				MyFolderMaps.get(folder).create(true, true, monitor);
			}

			Date date=new Date();
			Map<String, Object> paramMap=new HashMap<String,Object>();
			
			//the parameters used by velocity
			paramMap.put("templatePath", templatePathString);
			paramMap.put("projectName", proj.getName());
			paramMap.put("createTime", date.toString());
			paramMap.put("PROJECTNAME", proj.getName().toUpperCase());
			paramMap.put("author", authorString);
			paramMap.put("toBeDone", "add your code here");
			
			
			for(String str:fileKeySets){
				paramMap.put("templateFile", str);
				paramMap.put("outputFile", files.get(str));

				addFile(container, monitor, paramMap);	
			}
				
		} catch (IOException ioe) {
			IStatus status = new Status(IStatus.ERROR, "NewFileWizard", IStatus.OK,
					ioe.getLocalizedMessage(), null);
			throw new CoreException(status);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			monitor.done();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		this.workbench = workbench;
	}

	/**
	 * Sets the initialization data for the wizard.
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		this.config = config;
	}

	/**
	 * Adds a new file to the project.
	 * 
	 * @param container
	 * @param path
	 * @param contentStream
	 * @param monitor
	 * @throws CoreException
	 */
	private int addFileToProject(IContainer container, Path path,
			InputStream contentStream, IProgressMonitor monitor) {

		final IFile file = container.getFile(path);
		try {
			if (file.exists()) {

				file.setContents(contentStream, true, true, monitor);

			} else {
				file.create(contentStream, true, monitor);
			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			return -1;
		}
		return 0;
	}

}
