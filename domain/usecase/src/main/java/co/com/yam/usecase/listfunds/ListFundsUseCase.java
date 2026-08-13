package co.com.yam.usecase.listfunds;

import co.com.yam.model.fund.Fund;
import co.com.yam.model.fund.gateways.FundRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class ListFundsUseCase {

    private final FundRepository fundRepository;

    public Flux<Fund> list() {
        return fundRepository.findAll();
    }
}
