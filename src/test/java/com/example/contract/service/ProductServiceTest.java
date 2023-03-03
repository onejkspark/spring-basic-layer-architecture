package com.example.contract.service;

import com.example.contract.exception.AppException;
import com.example.contract.domain.product.Product;
import com.example.contract.domain.warrant.Warrant;
import com.example.contract.controller.request.ProductSaveRequest;
import com.example.contract.dto.mapper.EstimatedPremium;
import com.example.contract.mock.EstimatedPremiumImpl;
import com.example.contract.mock.args.SampleArgs;
import com.example.contract.repository.ProductRepository;
import com.example.contract.repository.WarrantRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.example.contract.mock.ConvertUtil.convert;
import static com.example.contract.mock.ConvertUtil.convertProduct;
import static com.example.contract.mock.MockUtil.readJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("상품 서비스 레이어 에서")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarrantRepository warrantRepository;

    @BeforeEach
    public void init() {
        productService = new ProductService(productRepository, warrantRepository);
    }

    @Nested
    @DisplayName("저장 로직 은")
    class CreatedMethod {

//        @MethodSource("getArgs")
        Stream<Arguments> getArgs() {
            return Stream.of(
                Arguments.of(1),
                Arguments.of(2)
            );
        }

        @DisplayName("파라미터 테스트 시나리오 입니다.")
        @ParameterizedTest(name = "{0} 을 집어 넣습니다.")
        @ArgumentsSource(SampleArgs.class)
        public void param(int n) {
            assertTrue(n != 0);
        }

        @Test
        @DisplayName("성공적으로 저장을 하게 된다.")
        public void created_ok() {

            Map map = readJson("json/product/service/created_ok.json", Map.class);

            Set<Warrant> warrantSet = convert(map.get("warrant"));

            Product mock = convert(convertProduct((Map) map.get("product")), warrantSet);

            given(warrantRepository.findByIdIn(any())).willReturn(warrantSet);

            given(productRepository.save(any())).willReturn(mock);

            ProductSaveRequest dto = readJson("json/product/service/product_save_request.json", ProductSaveRequest.class);

            Product entity = productService.created(dto);

            then(warrantRepository).should().findByIdIn(any());
            then(productRepository).should().save(any());

            assertEquals(entity.getTitle(), mock.getTitle());
            assertEquals(entity.getTerm(), mock.getTerm());
            assertEquals(entity.getWarrants(), mock.getWarrants());
        }

        @Test
        @DisplayName("담보 데이터가 없다면, AppException 이 발생이 된다.")
        public void created_fail1() {

            given(warrantRepository.findByIdIn(any())).willReturn(new HashSet<>());

            ProductSaveRequest dto = readJson("json/product/service/product_save_request.json", ProductSaveRequest.class);

            assertThrows(AppException.class, () -> {
                productService.created(dto);
            });
        }
    }

    @Nested
    @DisplayName("예상 총보험료 로직 은")
    class GetEstimatedPremiumMethod {

        @Test
        @DisplayName("담보 아이디들이 있을 때 정상적으로 조회가 된다.")
        public void getEstimatedPremium_ok1() {

            Optional<EstimatedPremium> mockOptional = Optional.of(
                new EstimatedPremiumImpl(readJson("json/product/service/getEstimatedPremium_ok1.json", Product.class)));

            given(productRepository.findByIdAndWarrants_IdIn(anyLong(), any(), eq(EstimatedPremium.class))).willReturn(mockOptional);

            Map<String, Object> dto = readJson("json/product/service/getEstimatedPremium_ok1_dto.json", Map.class);

            List<Long> warrantIds = ((ArrayList<Integer>) dto.get("warrantIds")).stream().map(Integer::longValue).collect(
                Collectors.toList());

            Integer productId = (Integer) dto.get("productId");

            Optional<EstimatedPremium> entityOptional = productService.getEstimatedPremium(productId.longValue(), warrantIds);

            then(productRepository).should().findByIdAndWarrants_IdIn(anyLong(), any(), eq(EstimatedPremium.class));

            EstimatedPremium entity = entityOptional.get();

            EstimatedPremium mock = mockOptional.get();

            assertEquals(entity.getProductTitle(), mock.getProductTitle());
            assertEquals(entity.getTerm(), mock.getTerm());
            assertEquals(entity.getPremium(), mock.getPremium());
        }

        @Test
        @DisplayName("담보 아이디들이 없을 때 정상적으로 조회가 된다.")
        public void getEstimatedPremium_ok2() {

            Optional<EstimatedPremium> mockOptional = Optional.of(
                new EstimatedPremiumImpl(readJson("json/product/service/getEstimatedPremium_ok2.json", Product.class)));

            given(productRepository.findById(anyLong(), eq(EstimatedPremium.class))).willReturn(mockOptional);

            Map<String, Object> dto = readJson("json/product/service/getEstimatedPremium_ok2_dto.json", Map.class);

            List<Long> warrantIds = ((ArrayList<Integer>) dto.get("warrantIds")).stream().map(Integer::longValue).collect(
                Collectors.toList());

            Integer productId = (Integer) dto.get("productId");

            Optional<EstimatedPremium> entityOptional = productService.getEstimatedPremium(productId.longValue(), warrantIds);

            then(productRepository).should().findById(anyLong(), eq(EstimatedPremium.class));

            EstimatedPremium entity = entityOptional.get();

            EstimatedPremium mock = mockOptional.get();

            assertEquals(entity.getProductTitle(), mock.getProductTitle());
            assertEquals(entity.getTerm(), mock.getTerm());
            assertEquals(entity.getPremium(), mock.getPremium());
        }
    }

}
