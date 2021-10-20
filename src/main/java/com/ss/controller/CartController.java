package com.ss.controller;

//import java.sql.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ss.model.Cart;
import com.ss.model.Customer;
import com.ss.model.Product;
import com.ss.model.Purchase;
import com.ss.service.CartService;
import com.ss.service.CustomerService;
import com.ss.service.PurchaseService;

@Controller
public class CartController {
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private PurchaseService purchaseService;
	
	@ExceptionHandler(Exception.class)
	public String handleSqlException(Exception e, HttpSession session) {
		session.setAttribute("action", "Choose Payment before Buying");
		return "redirect:/viewCart";
	}
	@PostMapping("/confirmCart")
	public String addToCart(@RequestParam("size") float size,@RequestParam("quantity") int quantity,HttpSession session) {
		Cart cart = new Cart();
		Product product = (Product) session.getAttribute("product");
		int min=100;int max=999;int b = (int)(Math.random()*(max-min+1)+min);
		cart.setId(b);
		cart.setProductId(product.getId());
		cart.setQuantity(quantity);
		cart.setPrice(product.getPrice()*quantity);
		cart.setSize(size);
		cartService.saveCart(cart);
		session.setAttribute("action", "Product added to cart");
		float temp=0;
		if(session.getAttribute("sessionCost")==null) {
			temp=0;
		}else {
			temp=(float) session.getAttribute("sessionCost");
		}
		float sessionCost=(cart.getPrice()+temp);
		session.setAttribute("sessionCost", sessionCost);
		return "redirect:/";
	}
	
	@GetMapping("/viewCart")
	public String viewCart(Model model,HttpSession session) {
		List<Cart> cartList = cartService.getAllCart();
		if(!cartList.isEmpty()) {
		model.addAttribute("cartList", cartList);
		model.addAttribute("action", session.getAttribute("action"));
		session.setAttribute("action", null);
		return "viewCart";
		}else {
			session.setAttribute("action", "No products currently in Cart");
			return "redirect:/";
		}
	}
	@GetMapping("/deleteProductCart/{id}")
	public String deleteProduct(@PathVariable(value="id") int id,Model model,HttpSession session) {
		cartService.deleteProductInCart(id);
		session.setAttribute("action", "Product Deleted Succesfully");
		Cart product = new Cart();
		model.addAttribute("product", product);
		return "viewCart";
	}
	@PostMapping("/buyNow")
	public String buyProducts(@RequestParam("name_on_card") String pm,@RequestParam("card_details") String pm1, HttpSession session) {
		System.out.println(pm);
		if(!((pm.equals(""))||(pm1.equals("")))) {
			List<Cart> cartList = cartService.getAllCart();
			Purchase purchase = new Purchase();
			String email = (String) session.getAttribute("customerLogin");
			Customer customer = customerService.getCustomer(email);
			for(Cart cl:cartList) {
				java.sql.Date date = new java.sql.Date(new java.util.Date().getTime());
				int min=100000;int max=999999;int b = (int)(Math.random()*(max-min+1)+min);
				purchase.setId(b);
				purchase.setDop(date);
				System.out.println(date);
				purchase.setCustomer(customer);
				purchase.setProductid(cl.getProductId());
				purchase.setQuantity(cl.getQuantity());
				purchase.setTotalcost(cl.getPrice());
				purchase.setSize(cl.getSize());
				purchaseService.addPurchase(purchase);
			}
		session.setAttribute("action", "Payment successful and Products added to Customer Order List Sucessfully");
		return "redirect:/";
		}else {
			session.setAttribute("action", "Enter correct payment detalils");
			return "redirect:/viewCart";
		}	
	}
	
	
	
	@GetMapping("/deleteCartItems/{id}")
	public String deleteCartItems(@PathVariable("id") int id,Model model,HttpSession session) {
	
		float temp=0;
		float Cost=cartService.getProductById(id).getPrice();
		temp=(float) session.getAttribute("sessionCost");
		float sessionCost= temp-Cost;
		session.setAttribute("sessionCost", sessionCost);
		model.addAttribute("action", "Cart item Deleted Succesfully");
		cartService.deleteProductInCart(id);
		return "redirect:/viewCart";
	}
		
}
