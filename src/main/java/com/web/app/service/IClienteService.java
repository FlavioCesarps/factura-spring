package com.web.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.web.app.entity.Cliente;
import com.web.app.entity.Factura;
import com.web.app.entity.Producto;

public interface IClienteService {

	public List<Cliente> findAll();
	
	public Page<Cliente> findAll( Pageable pageable);
	
	public void save(Cliente cliente);
	
	public Cliente findOne(Long id);
	
	public void delete(Long id );
	
	public List<Producto> finByNombre(String termino);
	
	public void saveFactura(Factura factura);
	
	public Producto findProductoById(Long id);
	
	public Factura findFacturaById(Long id);
	
	public void deleteFacturar(Long id);
	
	public Factura fetchFacturaByWithClienteWhiteItemFacturaWithProducto(Long id);
	
	public Cliente fetchByIdWithFacturas(Long id);
	
}
