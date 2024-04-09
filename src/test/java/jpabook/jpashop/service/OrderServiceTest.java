package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @PersistenceContext EntityManager em;

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception{
        //given
        Member member = createMember();
        Item book = createBook("정처기",10000,10);
        int orderCount = 2; //주문수량

        //When
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        Assert.assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        Assert.assertEquals("주문한 상품 종류 수가 정확해야한다",1,getOrder.getOrderItems().size());
        Assert.assertEquals("주문 가격은 가격 * 수량이다.",10000 * 2,getOrder.getTotalPrice());
        Assert.assertEquals("주문 수량 만큼 재고가 줄어야한다.",8,book.getStockQuantity());
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception{
        Member member = createMember();
        Item book = createBook("정처기",10000,10);

        int orderCount = 11; //10개의 재고보다 많음

        //when
        orderService.order(member.getId(), book.getId(),orderCount);

        fail("재고 수량 부족 예외가 발생해야한다..");
    }

    @Test
    public void 주문_취소() throws Exception{
        Member member = createMember();
        Item book = createBook("정처기",10000,10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        Assert.assertEquals("상태가 CANCEL상태여야함.",OrderStatus.CANCEL,getOrder.getStatus());
        Assert.assertEquals("재고가 다시 10개로 변해야함.",10,book.getStockQuantity());

    }

    private Member createMember(){
        Member member = new Member();
        member.setName("임채환");
        member.setAddress(new Address("광주","광산구", "121-21"));
        em.persist(member);

        return member;
    }

    private Book createBook(String name,int price,int stockQuantity){
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }
}