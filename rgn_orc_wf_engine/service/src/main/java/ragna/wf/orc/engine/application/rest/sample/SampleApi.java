package ragna.wf.orc.engine.application.rest.sample;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/functional-hello")
public class SampleApi {
  @GetMapping("/response-mono")
  public ResponseEntity<Mono<String>> helloMono() {
    return ResponseEntity.ok(Mono.fromSupplier(() -> "Hello SpringFox!"));
  }

  @GetMapping("/mono")
  public Mono<String> helloPerson(String name) {
    return Mono.just("Hello " + name + "!");
  }

  @GetMapping("/flux")
  public Flux<String> helloPeople(String... names) {
    return Flux.fromArray(names).map(name -> "Hello " + name + "!");
  }

  @GetMapping("/response-flux")
  public ResponseEntity<Flux<String>> helloPeople(@RequestParam List<String> names) {
    return ResponseEntity.of(
        Optional.of(Flux.fromStream(names.stream().map(name -> "Hello " + name + "!"))));
  }
}
