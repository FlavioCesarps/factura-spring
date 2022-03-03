package com.web.app.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import com.web.app.entity.Cliente;
import com.web.app.paginator.PageRender;
import com.web.app.service.IClienteService;
import com.web.app.service.IUploadFileService;

@Controller
@SessionAttributes("cliente")
public class ClienteController {
	
	protected final Log logger = LogFactory.getLog(this.getClass());
	
	@Autowired
	private IClienteService clienteService;
	
	@Autowired
	private IUploadFileService uploadFileService;
	
	@Autowired
	private MessageSource messageSource;
	
	@Secured({"ROLE_USER", "ROLE_ADMIN"})
	@GetMapping(value = "/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {

		Resource recurso = null;

		try {
			recurso = uploadFileService.load(filename);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}
	
	
	@GetMapping(value = "/listarRest")
	public @ResponseBody List<Cliente> listarRest() {
		
		return  clienteService.findAll();
		
	}
	
	
	
	@GetMapping(value = {"/listar", "/"})
	public String listarCliente(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			Authentication authentication, HttpServletRequest request, Locale locale) {
		
		if ( authentication != null ) {
			logger.info("Hola usuario autenticado, tu username es :" + authentication.getName());
		}
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if( auth !=null ) {
			logger.info("Utilizando forma estatica SecurityContextHolder.getContext().getAuthentication(): Hola usuario atenticado, tu username es : " + auth.getName());
		}
		
		if( hasRole("ROLE_ADMIN") ) {
			logger.info("Hola "+ auth.getName() + " tienes acceso!");
		}else {
			logger.info("Hola "+ auth.getName() + " no tienes acceso vete!");
		}
		
		SecurityContextHolderAwareRequestWrapper securityContext = new SecurityContextHolderAwareRequestWrapper(request, "");
		
		if( securityContext.isUserInRole("ROLE_ADMIN") ) {
			logger.info("Hola forma SecurityContextHolderAwareRequestWrapper" + auth.getName()+ " tienes acceso");	
		}else {
			logger.info("Hola forma SecurityContextHolderAwareRequestWrapper" + auth.getName()+ " tienes acceso");	
		}
		
		if( request.isUserInRole("ROLE_ADMIN") ) {
			logger.info("Hola forma HttpServletRequest" + auth.getName()+ " tienes acceso");	
		}else {
			logger.info("Hola forma HttpServletRequest" + auth.getName()+ " tienes acceso");	
		}
		
		Pageable pageRequest = PageRequest.of(page, 4);
		
		Page<Cliente> clientes = clienteService.findAll(pageRequest);
		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);
		
		model.addAttribute("titulo", messageSource.getMessage("text.cliente.listar.titulo", null,locale));
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		
		return "listar";
	}
	
	@Secured("ROLE_ADMIN")
	@GetMapping(value = "/form")
	public String crearCliente(Model model) {
		
		Cliente cliente = new Cliente();
		model.addAttribute("cliente",cliente);
		model.addAttribute("titulo", "Formulario del Cliente");
		
		return "form";
	}
	
	@Secured("ROLE_ADMIN")
		@RequestMapping(value = "/form", method = RequestMethod.POST)
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Cliente");
			return "form";
		}

		if (!foto.isEmpty()) {

			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
					&& cliente.getFoto().length() > 0) {

				uploadFileService.delete(cliente.getFoto());
			}

			String uniqueFilename = null;
			try {
				uniqueFilename = uploadFileService.copy(foto);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			flash.addFlashAttribute("info", "Has subido correctamente '" + uniqueFilename + "'");

			cliente.setFoto(uniqueFilename);
		}

		String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con éxito!" : "Cliente creado con éxito!";

		clienteService.save(cliente);
		status.setComplete();
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:listar";
	}
		
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping(value = "/form/{id}")
	public String editarCliente( @PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		
		Cliente cliente = null;
		
		if( id > 0 ) {
			cliente = clienteService.findOne(id);
			
			if ( cliente == null ) {
				flash.addFlashAttribute("error", "El id deel cliente no existe");
				return "redirect:/listar";
			}
			
		}else {
			flash.addFlashAttribute("error", "El id del cliente no puede ser cero");
			return "redirect:/listar";
		}
		
		model.addAttribute("cliente",cliente);
		model.addAttribute("titulo", "Editar Cliente");
		
		return "form";
	}

	@Secured("ROLE_ADMIN")
	@GetMapping( value = "/eliminar/{id}" )
	public String eliminarCliente(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		
		if (id > 0) {
			Cliente cliente = clienteService.findOne(id);

			clienteService.delete(id);
			flash.addFlashAttribute("success", "Cliente eliminado con éxito!");

			if (uploadFileService.delete(cliente.getFoto())) {
				flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada con exito!");
			}

		}
		return "redirect:/listar";
	}
	
	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping(value = "/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {

		Cliente cliente = clienteService.fetchByIdWithFacturas(id);
		if (cliente == null) {
			flash.addFlashAttribute("error", "El cliente no existe en la base de datos");
			return "redirect:/listar";
		}

		model.addAttribute("cliente", cliente);
		model.addAttribute("titulo", "Detalle cliente: " + cliente.getNombre());
		return "ver";
	}
	
	
	private boolean hasRole(String role) {
		
		SecurityContext context = SecurityContextHolder.getContext();
		
		if( context == null ) {
			return false;
		}
		
		Authentication auth = context.getAuthentication();
		if( auth == null ) {
			return false;
		}
		
		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
		
		return authorities.contains(new SimpleGrantedAuthority(role));
		
		/** for (GrantedAuthority authority: authorities ) {
			if( role.equals(authority.getAuthority()) ) {
				logger.info("Hola usuario "+ auth.getName() + " tienes rol es: " + authority.getAuthority());
				return true;
			}
		}
		
		return false; **/
	}
	
}
