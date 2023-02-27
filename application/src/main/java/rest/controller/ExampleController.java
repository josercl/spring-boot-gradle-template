package rest.controller;

import my.group.rest.server.api.PersonsApi;
import my.group.rest.server.model.CreatePersonDTO;
import my.group.rest.server.model.PersonDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@RestController
public class ExampleController implements PersonsApi {

    @Override
    public ResponseEntity<List<PersonDTO>> listPersons(Pageable pageable) {
        int min = pageable.getPageNumber() * pageable.getPageSize();
        int max = min + pageable.getPageSize();
        List<PersonDTO> list = IntStream.range(min, max)
                .mapToObj(i -> new PersonDTO()
                        .id(Long.parseLong("" + (i + 1)))
                        .name("Name " + i)
                        .age(30 + i)
                        .phoneNumber("phoneNumber " + i)
                ).toList();
        return ResponseEntity.ok(list);
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
