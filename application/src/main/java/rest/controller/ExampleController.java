package rest.controller;

import my.group.rest.server.api.PersonsApi;
import my.group.rest.server.model.CreatePersonDTO;
import my.group.rest.server.model.GenericPageMetaDTO;
import my.group.rest.server.model.PersonDTO;
import my.group.rest.server.model.PersonPageDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.stream.IntStream;

@RestController
public class ExampleController implements PersonsApi {
    @Override
    public ResponseEntity<PersonPageDTO> listPersons(Pageable pageable) {
        int min = pageable.getPageNumber() * pageable.getPageSize();
        int max = min + pageable.getPageSize();

        return ResponseEntity.ok(
            new PersonPageDTO()
                .meta(
                    new GenericPageMetaDTO()
                        .page(pageable.getPageNumber())
                        .size(pageable.getPageSize())
                        .totalPages(4)
                        .totalElements(200)
                )
                .data(
                    IntStream.range(min, max)
                        .mapToObj(i -> new PersonDTO()
                            .id(Long.parseLong("" + (i + 1)))
                            .name("Name " + i)
                            .age(30 + i)
                            .phoneNumber("phoneNumber " + i)
                        ).toList()
                )
        );
    }

    @Override
    public ResponseEntity<PersonDTO> createPerson(CreatePersonDTO createPersonDTO) {
        return ResponseEntity.ok(new PersonDTO()
            .phoneNumber(createPersonDTO.getPhoneNumber())
            .age(createPersonDTO.getAge())
            .name(createPersonDTO.getName())
            .id(new Random().nextLong()));
    }
}
