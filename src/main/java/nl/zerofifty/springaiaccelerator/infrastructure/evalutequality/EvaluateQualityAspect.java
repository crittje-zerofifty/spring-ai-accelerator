package nl.zerofifty.springaiaccelerator.infrastructure.evalutequality;

import nl.zerofifty.springaiaccelerator.infrastructure.adapter.EvaluateResultAdapter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Aspect
@Component
@Profile("eval-testing") // Het aspect bestaat alleen als dit profiel actief is
public class EvaluateQualityAspect {

    private static final Logger log = LoggerFactory.getLogger(EvaluateQualityAspect.class);

    private final EvaluateResultAdapter evaluateResultAdapter;

    public EvaluateQualityAspect(EvaluateResultAdapter evaluateResultAdapter) {
        this.evaluateResultAdapter = evaluateResultAdapter;
    }

    @Around("@annotation(nl.zerofifty.springaiaccelerator.application.annotation.EvaluateQuality)")
    public Object evaluate(ProceedingJoinPoint joinPoint) throws Throwable {
        String prompt = (String) joinPoint.getArgs()[0];

        Object result = joinPoint.proceed();

        if (result instanceof Flux) {
            return ((Flux<?>) result)
                    .collectList()
                    .flatMapMany(list -> {
                        String fullResponse = String.join("", list.stream().map(Object::toString).toList());
                        evaluateResultAdapter.evaluate(prompt, "CONTEXT_HOLDER", fullResponse);
                        return Flux.fromIterable(list);
                    });
        }

        return result;
    }
}
