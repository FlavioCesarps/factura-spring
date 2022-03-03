package com.web.app.view.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.web.app.entity.Cliente;

@XmlRootElement(name = "clientes")
public class ClienteList {
	
	
	@XmlElement(name = "cliente")
	public java.util.List<Cliente> clientes;
	
	public ClienteList(List<Cliente> clientes) {
		this.clientes = clientes;
	}

	public ClienteList() {
		
	}

	public java.util.List<Cliente> getClientes() {
		return clientes;
	}
	
	
	
}
