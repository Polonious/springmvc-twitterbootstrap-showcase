package org.springframework.samples.mvc.wikitext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/wikitext")
public class WikitextController {

	private static String wikiHome = "/home/polonious/wikitext";
	
	@RequestMapping(method=RequestMethod.GET, value = "/{id}/{filename:.+}")
	public void download(@PathVariable("id") String id, 
			@PathVariable("filename") String filename, 
			HttpServletResponse response) throws FileNotFoundException, IOException {
		File pdfFile = new File(wikiHome+"/tmp/wikitext"+id+".pdf");
		pdfFile.deleteOnExit();
		
		if(pdfFile.exists()){
			response.setContentType("application/pdf");
	        response.setContentLength(Long.valueOf(pdfFile.length()).intValue());
	        response.setHeader("Content-Disposition","attachment; filename=\""+filename+"\"");
	        FileCopyUtils.copy(new FileInputStream(pdfFile), 
                        response.getOutputStream());
        }
	}

	@RequestMapping(method=RequestMethod.POST)
	@ResponseBody
	public String processUpload(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "title", defaultValue = "Documentation") String title,
			HttpServletResponse response) throws IOException {
		File wikitextFile = File.createTempFile("wikitext", ".textile", 
				new File(wikiHome+"/tmp"));
		wikitextFile.deleteOnExit();
		file.transferTo(wikitextFile);

		String name = FilenameUtils.getBaseName(wikitextFile.getAbsolutePath());
		
		File buildFile = new File(wikiHome+"/build.xml");
		Project project = new Project();
		project.setUserProperty("ant.file", buildFile.getAbsolutePath());
		project.setProperty("document.name", name);
		project.setProperty("bookTitle", URLDecoder.decode(title, "UTF-8"));
		project.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		project.addReference("ant.projectHelper", helper);
		helper.parse(project, buildFile);
		project.executeTarget(project.getDefaultTarget());
		
		return name.substring(8);
	}
	
}
