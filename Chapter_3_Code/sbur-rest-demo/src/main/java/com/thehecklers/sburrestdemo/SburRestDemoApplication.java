package com.thehecklers.sburrestdemo;

import java.util.*;
import javax.persistence.*; //@Entity 어노테이션에 필요

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
//어노테이션에 필요한 import 구문
import org.springframework.web.bind.annotation.*;
//레포지토리에 필요한 import 구문
import org.springframework.data.repository.*;



//도메인 클래스 생성 - 커피 도메인
/* 
도메인 생성에 필요한 조건
1. 메인 함수를 포함하는 클래스
2. 멤버 변수, 생성자, get/set 함수를 생성하는 클래스 
*/

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
@RequestMapping("/coffees") //각 메서드에 바복되는 매핑 url 제거
class RestApiDemoController {
	private List<Coffee> coffees = new ArrayList<>();
	
	public RestApiDemoController () {
		coffees.addAll(List.of(
				new Coffee ("에스프레소"),
				new Coffee ("아메리카노"),
				new Coffee ("바닐라라떼")
		));
	}
	
	//get
	//@GetMapping 어노테이션은 get 요청만 허용, URL 경로만 지정
	//가독성 좋음
	@GetMapping
    Iterable<Coffee> getCoffees() {
        return coffees;
    }
	/* @RequestMapping 어노테이션으로도 변경 가능
	@RequestMapping (value = "/coffees", method = RequestMethod.GET)
	 */
	
	@GetMapping("/{id}")
    Optional<Coffee> getCoffeeById(@PathVariable String id) {
		for (Coffee c: coffees) {
			if (c.getId().equals(id)) {
				return Optional.of(c);
			}
		}
		return Optional.empty();
    }
	
	//post
	//Coffee 객체는 스프링 부트에 의해 언마샬링되어 요청한 애플리케이션이나 서비스로 반환
	@PostMapping
	Coffee postCoffee (@RequestBody Coffee coffee) {
		coffees.add(coffee);
		return coffee;
	}
	
	//put
	@PutMapping ("/{id}")
	//파악된 url를 통해 기존 리소스를 업데이트 및 생성된 Coffee 객체 반환
	/*
	Coffee putCoffee(@PathVariable String id, @RequestBody Coffee coffee) {
		int coffeeIndex = -1;
		
		// 특정 식별자로 커피를 검색해, 찾으면 업데이트
		for (Coffee c: coffees) {
			if (c.getId().equals(id)) {
				coffeeIndex = coffees.indexOf(c);
				coffees.set(coffeeIndex,  coffee);
			}
		}
		return (coffeeIndex == -1) ? postCoffee(coffee) : coffee;
	}
	*/
	//해당 객체와 적절한 HTTP 상태 코드가 포함된 ResponseEntity 반환
	//HTTP 상태 코드는 데이터 저장소에 커피가 존재하지 않는 경우 201(Created), 커피가 존재하는 경우 200(OK)를 반환
	ResponseEntity<Coffee> putCoffee(@PathVariable String id, @RequestBody Coffee coffee) {
		int coffeeIndex = -1;
		
		// 특정 식별자로 커피를 검색해, 찾으면 업데이트
		for (Coffee c: coffees) {
			if (c.getId().equals(id)) {
				coffeeIndex = coffees.indexOf(c);
				coffees.set(coffeeIndex,  coffee);
			}
		}
		return (coffeeIndex == -1) ?
				new ResponseEntity<>(postCoffee(coffee), HttpStatus.CREATED) :
					new ResponseEntity<>(coffee, HttpStatus.OK);
	}
	
	//delete
	//커피 식별자인 id를 받아 Collection 메서드의 removeIf로 해당 커피를 목록에서 제거
	@DeleteMapping ("/{id}")
	void deleteCoffee(@PathVariable String id) {
		coffees.removeIf(c-> c.getId().equals(id));
	}
}

// 스프링 데이터 Repository 인터페이스를 상속할 인터페이스 정의
interface CoffeeRepository extends CrudRepository <Coffee, String> {
}
