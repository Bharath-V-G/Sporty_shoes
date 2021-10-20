package com.ss.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ss.model.Cart;
import com.ss.model.Product;

public interface CartRepository extends JpaRepository<Cart, Integer>{
	
	Cart findById(int id);
	@Query("Select c.price from Cart c where c.id Like %?1%")
	public float getPriceById(int id);

}
