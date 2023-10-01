package com.thehecklers.sburrestdemo;

import java.util.*;
import javax.persistence.*; //@Entity 어노테이션에 필요

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
//annotation 에 필요한 import 구문
import org.springframework.web.bind.annotation.*;
//repository 에 필요한 import 구문
import org.springframework.data.repository.*;
//@component에 필요한 import 구문
import org.springframework.stereotype.*;
//@postConstruct에 필요한 import 구문
import jakarta.annotation.PostConstruct;


// 스프링 데이터 Repository 인터페이스를 상속할 인터페이스 정의
// CrudRepository의 유연한 API에서 생성된 repository 빈을 활용
interface CoffeeRepository extends CrudRepository <Coffee, String> {
}


@SpringBootApplication
public class SburRestDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SburRestDemoApplication.class, args);
	}
}

@Entity
class Coffee {
	@Id // 기존 id 멤버 변수를 DB 테이블의 ID 필드로 표시하기 위해 @Id 어노테이션 추가
	// 멤버변수 생성
	private String id;
	private String name;
	
	// 생성자
	public Coffee (String id, String name) {
		this.id = id;
		this.name = name;
	}
	public Coffee (String name) {
		this(UUID.randomUUID().toString(), name);
	}
	// 기본 생성자 : JPA를 사용해 DB에 데이터 생성 시 필요
	public Coffee () {
	}
	
	//get 함수 생성
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	
	//set 함수 생성
	public void setName(String name) {
		this.name = name;
	}
	public void setID(String id) {
		this.id = id;
	}
}
// 기본적인 도메인 생성 완료

@RestController
@RequestMapping("/coffees") //각 메서드에 반복되는 매핑 url 제거
class RestApiDemoController {
	private final CoffeeRepository coffeeRepository;
	
	public RestApiDemoController (CoffeeRepository coffeeRepository) {
		this.coffeeRepository = coffeeRepository;
	}
	
	//get
	//coffeeRepository.findAll()을 호출하면 모든 커피 항목이 반환
	@GetMapping
    Iterable<Coffee> getCoffees() {
        return coffeeRepository.findAll();
    }
	@GetMapping("/{id}")
    Optional<Coffee> getCoffeeById(@PathVariable String id) {
		return coffeeRepository.findById(id);
    }
	
	//post
	@PostMapping
	Coffee postCoffee (@RequestBody Coffee coffee) {
		return coffeeRepository.save(coffee);
	}
	
	//put
	@PutMapping ("/{id}")
	// existById() 메서드로 새로운 커피 데이터인지, 저장된 커피 데이터인지 확인 후 저장된 커피 데이터와 적절한 HTTP 상태 코드 반환
	ResponseEntity<Coffee> putCoffee(@PathVariable String id, @RequestBody Coffee coffee) {
		/* 
		 repository 를 사용하도록 메서드를 리팩터링한 후에는 부정 조건을 먼저 평가할 이유가 없으므로 
		 NOT(!) 논리 연산자를 제거하고 원래 결과를 유지하기 위해 삼항 연산자의 참과 거짓 값을 바꿔 줌
		 */
		return (coffeeRepository.existsById(id)) 
				? new ResponseEntity<>(coffeeRepository.save(coffee), HttpStatus.OK) 
						: new ResponseEntity<>(coffeeRepository.save(coffee), HttpStatus.CREATED);
	}
	
	//delete
	@DeleteMapping ("/{id}")
	void deleteCoffee(@PathVariable String id) {
		coffeeRepository.deleteById(id);
	}
}

@Component
class DataLoader {
	private final CoffeeRepository coffeeRepository;
	
	public DataLoader (CoffeeRepository coffeeRepository) {
		this.coffeeRepository = coffeeRepository;
	}
	
	@PostConstruct
	private void loadData() {
		this.coffeeRepository.saveAll (List.of(
				new Coffee ("에스프레소"),
				new Coffee ("아메리카노"),
				new Coffee ("바닐라라떼")
		));
	}
}
	
	