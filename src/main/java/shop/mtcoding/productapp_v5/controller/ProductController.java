package shop.mtcoding.productapp_v5.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import shop.mtcoding.productapp_v5.dto.product.ProductReqDto.ProductSaveDto;
import shop.mtcoding.productapp_v5.dto.product.ProductReqDto.ProductUpdateDto;
import shop.mtcoding.productapp_v5.enums.ResponseEnum;
import shop.mtcoding.productapp_v5.handler.exception.CustomApiException;
import shop.mtcoding.productapp_v5.handler.exception.CustomException;
import shop.mtcoding.productapp_v5.model.paging.Criteria;
import shop.mtcoding.productapp_v5.model.paging.PagingVO;
import shop.mtcoding.productapp_v5.model.product.Product;
import shop.mtcoding.productapp_v5.model.product.ProductRepository;
import shop.mtcoding.productapp_v5.model.user.User;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private HttpSession session;

    // 상품 목록 페이지
    @GetMapping({ "/product", "/" })
    public String productList(Model model) {

        List<Product> productList = productRepository.findAll();
        model.addAttribute("productList", productList);
        return "product/productList";
    }

    // // 상품 목록 페이지
    // @GetMapping({ "/product", "/" })
    // public String productList(@RequestParam(required = false, defaultValue = "1")
    // int page, Criteria criteria,
    // Model model) {

    // List<Product> productList = productRepository.pagingProductList(criteria);

    // // 총 상품 갯수를 가져옴
    // int totalCount = productRepository.pagingProductCount();

    // // 페이징 처리를 위한 객체 생성
    // PagingVO pagingVO = new PagingVO();
    // pagingVO.setPage(page);
    // pagingVO.setTotal(totalCount);
    // // pagingVO.setSize(3); // 한 페이지당 10개씩 보여줌(필드에 이미 설정함)
    // pagingVO.setPageSize(5); // 페이지 번호는 5개씩 보여줌

    // model.addAttribute("productList", productList);
    // model.addAttribute("pagingVO", pagingVO);

    // return "product/productList";
    // }

    // 상품 상세 페이지
    @GetMapping("/product/{productId}")
    public String productDetail(@PathVariable Integer productId, Model model) {

        Product product = productRepository.findById(productId);
        model.addAttribute("product", product);

        return "product/productDetail";
    }

    // 상품 등록 페이지
    @GetMapping("/admin/productSave")
    public String productSave() {

        // 관리자 로그인 한 사람만 구매할 수 있음
        User principal = (User) session.getAttribute("principal");
        if (principal == null || !principal.getRole().equals("ADMIN")) {
            throw new CustomApiException(ResponseEnum.ADMIN_LOGIN_FAIL);
        }
        return "product/productSave";

    }

    // 상품 수정 페이지
    @GetMapping("/admin/productUpdate")
    public String productUpdate() {

        // 관리자 로그인 한 사람만 업데이트 할 수 있음
        User principal = (User) session.getAttribute("principal");
        if (principal == null || !principal.getRole().equals("ADMIN")) {
            throw new CustomApiException(ResponseEnum.ADMIN_LOGIN_FAIL);
        }

        return "product/productUpdate";
    }

    // 상품 등록
    @PostMapping("/admin/product/save")
    public String save(ProductSaveDto productSaveDto) {

        // 관리자 로그인 한 사람만 상품 등록할 수 있음
        User principal = (User) session.getAttribute("principal");
        if (principal == null || !principal.getRole().equals("ADMIN")) {
            throw new CustomApiException(ResponseEnum.ADMIN_LOGIN_FAIL);
        }

        // 유효성 체크
        if (productSaveDto.getProductName().isEmpty()) {
            // System.out.println("JoinCustomException - userName 실행됨");
            throw new CustomApiException(ResponseEnum.PRODUCT_NAME_EMPTY);
        }
        if (productSaveDto.getProductPrice() == null) {
            // System.out.println("JoinCustomException - userPassword 실행됨");
            throw new CustomApiException(ResponseEnum.PRODUCT_PRICE_EMPTY);
        }
        if (productSaveDto.getProductQty() == null) {
            // System.out.println("JoinCustomException - userEmail 실행됨");
            throw new CustomApiException(ResponseEnum.PRODUCT_QTY_EMPTY);
        }

        // 상품 금액, 재고 0개 이하 막기
        if (productSaveDto.getProductPrice() <= 0 || productSaveDto.getProductQty() <= 0) {
            throw new CustomApiException(ResponseEnum.PRODUCT_PRICE_QTY_ZERO);
        }

        // 기존 동일 상품 확인 (username,email만)
        if (productRepository.findByName(productSaveDto.getProductName()) != null) {
            throw new CustomApiException(ResponseEnum.PRODUCT_NAME_ALREADY_SAVED);
        }

        // 새로운 상품 등록(insert)
        int result = productRepository.insert(productSaveDto);

        // 디버깅
        System.out.println("상품 이름 : " + productSaveDto.getProductName());
        System.out.println("상품 가격 : " + productSaveDto.getProductPrice());
        System.out.println("상품 재고 : " + productSaveDto.getProductQty());

        // result 가 1이 아니면 업데이트 안된 것
        if (result != 1) {
            throw new CustomApiException(ResponseEnum.PRODUCT_UPDATE_FAIL);
        }
        // result == 1 업데이트 성공
        return "redirect:/product";
    }

    // 상품명 중복체크 컨트롤러
    @PostMapping("/productSave/checkName")
    public ResponseEntity<?> checkProductName(@RequestParam String productName) {

        // 디버깅
        System.out.println("productName : " + productName);

        // DB에 중복이 된 값이 있는 지 확인
        Product pn = productRepository.findByName(productName);

        if (pn != null) {
            // pn이 있다면 flase 반환
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
        // pn == null 기존에 없던 상품이기 때문에 true 반환
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    // 상품 수정 페이지
    @GetMapping("/admin/product/{productId}/updateForm")
    public String productUpdate(@PathVariable Integer productId, Model model) {

        // 관리자 로그인 한 사람만 상품 수정 가능
        User principal = (User) session.getAttribute("principal");
        if (principal == null || !principal.getRole().equals("ADMIN")) {
            throw new CustomApiException(ResponseEnum.ADMIN_LOGIN_FAIL);
        }

        // Product product = productRepository.findById(id);
        // model.addAttribute("product", product);
        Product product = productRepository.findById(productId);
        model.addAttribute("product", product);

        return "product/productUpdate";
    }

    // 상품 수정
    @PostMapping("/admin/product/{productId}/update")
    public String update(@PathVariable Integer productId, Model model, ProductUpdateDto productUpdateDto) {

        // 관리자 로그인 한 사람만 상품 수정 가능
        User principal = (User) session.getAttribute("principal");
        if (principal == null || !principal.getRole().equals("ADMIN")) {
            throw new CustomApiException(ResponseEnum.ADMIN_LOGIN_FAIL);
        }

        System.out.println("디버깅 : " + productId);
        Product p = productRepository.findById(productId);
        model.addAttribute("product", p);

        // 테스트
        System.out.println("p 아이디: " + p.getProductId());
        System.out.println("p 이름: " + p.getProductName());
        System.out.println("p 가격: " + p.getProductPrice());
        System.out.println("p 재고: " + p.getProductQty());

        Product product = new Product();
        product.setProductId(productUpdateDto.getProductId());
        product.setProductName(productUpdateDto.getProductName());
        product.setProductPrice(productUpdateDto.getProductPrice());
        product.setProductQty(productUpdateDto.getProductQty());
        System.out.println("데이터 담음");

        // 상품 금액, 재고 0개 이하 막기
        if (product.getProductPrice() <= 0 || product.getProductQty() <= 0) {
            throw new CustomApiException(ResponseEnum.PRODUCT_QTY_NO_MORE_THAN_ZERO);
        }

        // 업데이트
        int result = productRepository.update(product);

        // 테스트
        System.out.println("product 아이디: " + product.getProductId());
        System.out.println("product 이름: " + product.getProductName());
        System.out.println("product 가격: " + product.getProductPrice());
        System.out.println("product 재고: " + product.getProductQty());

        System.out.println("result : " + result);

        if (result != 1) {
            System.out.println("업데이트 실패");
            throw new CustomApiException(ResponseEnum.PRODUCT_UPDATE_FAIL);
        }
        System.out.println("업데이트 완료");

        return "redirect:/product/" + productId;
    }

    // 상품 삭제
    @PostMapping("/admin/product/{ProductId}/delete")
    public String delete(@PathVariable Integer ProductId) {

        // 관리자 로그인 한 사람만 상품 삭제 가능
        User principal = (User) session.getAttribute("principal");
        if (principal == null || !principal.getRole().equals("ADMIN")) {
            throw new CustomApiException(ResponseEnum.ADMIN_LOGIN_FAIL);
        }

        int result = productRepository.deleteById(ProductId);
        if (result != 1) {
            throw new CustomApiException(ResponseEnum.PRODUCT_DELETE_FAIL);
        }
        return "redirect:/product";
    }

}
