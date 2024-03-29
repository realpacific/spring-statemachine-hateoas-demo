# Spring Statemachine HATEOAS Demo

### State Diagram

```mermaid
stateDiagram-v2
    [*] --> DRAFT
    DRAFT --> SUBMITTED : AUTHOR_SUBMITTED
    SUBMITTED --> TE_APPROVED : TE_APPROVE
    SUBMITTED --> DRAFT : TE_REJECT
    TE_APPROVED --> PUBLISHED : FPE_APPROVE    
    TE_APPROVED --> DRAFT : FPE_REJECT    
    PUBLISHED --> [*]
```

![State Diagram](./state_diagram.png)

### Resources

* [How to Build Hypermedia API with Spring HATEOAS](https://grapeup.com/blog/how-to-build-hypermedia-api-with-spring-hateoas/)
* [Modelling business logic like a pro - Spring State Machine](https://www.wiktordyngosz.pl/16-05-2019-ssm/)
* [State machine](https://www.youtube.com/watch?v=M4Aa45Gpc4w)
* [GitHub Samples](https://github.com/spring-projects/spring-hateoas-examples/blob/main/spring-hateoas-and-spring-data-rest/README.adoc)
* [Concept and application of Spring Statemachine](https://programmer.ink/think/concept-and-application-of-spring-statemachine.html)
* [Docs](https://docs.spring.io/spring-statemachine/docs/current/reference/#statemachine-examples-persist)