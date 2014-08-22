package com.mywork.eclipse.wizards;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TemplateFileHandle {

	private Set<String> folderSet;
	private Map<String, String> fileMap;
	private String projectName;
	private String templatePath;
	
	
	public String getTemplateFile(String templatePath) {
		templatePath = templatePath.substring(this.templatePath.length() + 1);

		return templatePath;
	}

	public String getOutputFile(String templateName, String replace) {
		templateName = templateName.substring(this.templatePath.length() + 1);
		templateName = templateName.replaceAll(
				"\\$\\(Project\\)|\\$\\(project\\)", replace);
		return templateName.substring(0, templateName.length() - 3);
	}

	public void listAllFile(File file) {

		File files[] = file.listFiles();
		if (null != files) {
			for (File f : files) {
				if (f.isFile()) {
					if (f.getName().endsWith(".vm")) {
						fileMap.put(getTemplateFile(f.getPath()),
								getOutputFile(f.getPath(), this.projectName));
					}

				} else {
					folderSet.add(f.getPath().substring(
							this.templatePath.length() + 1));
				}
				listAllFile(f);
			}
		}
	}

	public TemplateFileHandle(String templatePath, String projectName) {
		super();
		folderSet = new TreeSet<String>();
		fileMap = new HashMap<String, String>();
		this.projectName = projectName;
		this.templatePath = templatePath;
		this.handleTemplatePath();

	}

	public void handleTemplatePath() {
		// TODO Auto-generated method stub
		File file = new File(this.templatePath);
		listAllFile(file);

		for (String str : folderSet) {
			System.out.println(str);
		}

		System.out.println("============================");
		Set<String> keySet = fileMap.keySet();
		for (String str : keySet) {
			System.out.println(str + ":" + fileMap.get(str));
		}
	}

	public Set<String> getFolderSet() {
		return folderSet;
	}

	public Map<String, String> getFileMap() {
		return fileMap;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getTemplatePath() {
		return templatePath;
	}

}
