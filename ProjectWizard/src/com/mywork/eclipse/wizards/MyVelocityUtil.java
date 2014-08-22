package com.mywork.eclipse.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

public class MyVelocityUtil {

	static int initFlag = 0;

	public static InputStream Parse(Map<String, Object> paramMap) {

		// System.out.println("enter the Parse function!");

		InputStream resultStream = null;

		try {

			System.out.println("MyVelocityUtil:"
					+ System.getProperty("user.dir"));// user.dir is the current
														// parth

				// use Properties init velocity
				Properties p = new Properties();
				
				// the location of velocity templates
				p.setProperty("file.resource.loader.path",
						(String) paramMap.get("templatePath"));
				p.setProperty("resource.loader", "file");
				p.setProperty("file.resource.loader.cache", "false");
				
				p.setProperty("input.encoding", "utf8");
	
				p.setProperty("output.encoding", "utf8");	
				
				//use VelocityEngine,not Velocity
				VelocityEngine velocity = new VelocityEngine(p);

			
			VelocityContext context = new VelocityContext();

			for (String key : paramMap.keySet()) {
				context.put(key, (String) paramMap.get(key));
			}

			org.apache.velocity.Template template = velocity
					.getTemplate((String) paramMap.get("templateFile"),"utf8");
			StringWriter writer = new StringWriter();
			template.merge(context, writer);

			resultStream = new ByteArrayInputStream(writer.toString()
					.getBytes());
			writer.flush();
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultStream;

	}
}
