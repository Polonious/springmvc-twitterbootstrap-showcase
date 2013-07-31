package org.springframework.samples.mvc.wikitext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.springframework.mvc.extensions.ajax.AjaxUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/wikitext")
public class WikitextController {

	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjaxRequest(request));
	}

	@RequestMapping(method=RequestMethod.GET)
	public void fileUploadForm() {
	}

	@RequestMapping(method=RequestMethod.POST)
	public void processUpload(@RequestParam MultipartFile file, HttpServletResponse response) throws IOException {
		File wikitextFile = File.createTempFile("wikitext", ".textile", new File("/home/polonious/wikitext"));
		String name = FilenameUtils.getBaseName(wikitextFile.getAbsolutePath());
		
		file.transferTo(wikitextFile);
		
		File buildFile = new File("/home/polonious/wikitext/build.xml");
		Project p = new Project();
		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
		p.setProperty("document.name", name);
		p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		helper.parse(p, buildFile);
		p.executeTarget(p.getDefaultTarget());
		
		File pdfFile = new File("/home/polonious/wikitext/"+name+".pdf");
		
		response.setContentType("application/pdf");
        response.setContentLength(Long.valueOf(pdfFile.length()).intValue());
        response.setHeader("Content-Disposition","attachment; filename=\"Documentation.pdf\"");
        FileCopyUtils.copy(new FileInputStream(pdfFile), 
                        response.getOutputStream());
	}
	
}
