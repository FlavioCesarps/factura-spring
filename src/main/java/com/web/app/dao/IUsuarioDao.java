package com.web.app.dao;

import org.springframework.data.repository.CrudRepository;

import com.web.app.entity.Usuario;

public interface IUsuarioDao extends CrudRepository<Usuario, Long>{

	public Usuario findByUsername(String username);
}