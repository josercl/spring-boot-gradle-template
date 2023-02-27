package rest.model.mapper;

import my.group.rest.server.model.GenericPageMetaDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PagedMapper {
    @Mapping(target = "page", source="pageable.pageNumber")
    @Mapping(target = "size", source="pageable.pageSize")
    @Mapping(target = "totalElements", source="numberOfElements")
    @Mapping(target = "totalPages", source="totalPages")
    @Named("metaFromPage")
    GenericPageMetaDTO fromPage(Page<?> page);
}
