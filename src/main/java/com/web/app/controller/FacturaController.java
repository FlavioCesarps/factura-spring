package com.web.app.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.web.app.entity.Cliente;
import com.web.app.entity.Factura;
import com.web.app.entity.ItemFactura;
import com.web.app.entity.Producto;
import com.web.app.service.IClienteService;

@Secured("ROLE_ADMIN")
@Controller
@RequestMapping("/factura")
@SessionAttributes("factura")
public class FacturaController {
	@Autowired
	private IClienteService clienteService;
	
	@GetMapping("/form/{clienteId}")
	public String crear(Model model, @PathVariable(value="clienteId") Long clienteId, RedirectAttributes flash) {
		
		Cliente cliente = clienteService.findOne(clienteId);
		
		if( cliente == null ) {
			flash.addFlashAttribute("error", "El cliente no existee en la base de datos");
			return "redirect:/listar";
		}
		
		Factura factura = new Factura();
		factura.setCliente(cliente);
		
		model.addAttribute("factura",factura);
		model.addAttribute("titulo", "Crear Factura");
		
		return "factura/form";
	}
	
	@GetMapping(value = "/cargar-productos/{term}", produces = {"application/json"})
	public @ResponseBody List<Producto> cargarProductos(@PathVariable String term){
		return clienteService.finByNombre(term);
	}
	
	@PostMapping(value = "/form")
	public String guardar(@Valid Factura factura, BindingResult result, Model model,
			@RequestParam(name = "item_id[]", required = false) Long[] itemId, @RequestParam(name = "cantidad[]", required = false) Integer[] cantidad,
			RedirectAttributes flash, SessionStatus status) {
		
		if( result.hasErrors() ) {
			model.addAttribute("titulo", "Crear Factura");
			return "factura/form";
		}
		
		if( itemId == null || itemId.length == 0 ) {
			model.addAttribute("error", "Error: La factura no puede tener líneas!");
			return "factura/form";
		}
		
		for (int i = 0; i < itemId.length; i++){
			Producto producto = clienteService.findProductoById(itemId[i]);
			
			ItemFactura linea = new ItemFactura();
			linea.setCantidad(cantidad[i]);
			linea.setProducto(producto);
			
			factura.addItemFactura(linea);
		}
		
		clienteService.saveFactura(factura);
		status.setComplete();
		
		flash.addFlashAttribute("success", "Factura creada con exito");
		
		return "redirect:/ver/"+ factura.getCliente().getId();
	}
	
	@GetMapping(value = "/ver/{id}")
	public String ver( @PathVariable(value = "id") Long id, Model model, RedirectAttributes flash ) {
		
		Factura factura = clienteService.fetchFacturaByWithClienteWhiteItemFacturaWithProducto(id);
		
		if( factura == null ) {
			flash.addAttribute("error", "La factura no existe en la base de datos");
			return "redirect:/listar";
		}
		
		model.addAttribute("factura", factura);
		model.addAttribute("titulo", "Factura: "+ factura.getDescripcion());
		
		return "factura/ver";
	}
	
	@GetMapping("/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		Factura factura = clienteService.findFacturaById(id);
		
		if( factura != null ) {
			clienteService.deleteFacturar(id);
			flash.addAttribute("success", "Factura Elimina con exito");
			return "redirect:/ver/" + factura.getCliente().getId();
		}
		
		flash.addAttribute("error", "La Factura no exites en la base de datos, no se pudo eliminar");
		
		return "redirect:/listar";
	
	}
}
