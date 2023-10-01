package baykov.daniel.springbootlibraryapp.payload.mapper;

import baykov.daniel.springbootlibraryapp.entity.Narrator;
import baykov.daniel.springbootlibraryapp.payload.dto.request.NarratorRequestDTO;
import baykov.daniel.springbootlibraryapp.payload.dto.response.NarratorResponseDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface NarratorMapper {

    NarratorMapper INSTANCE = Mappers.getMapper(NarratorMapper.class);

    @Mapping(target = "countryId", expression = "java(narrator.getCountry().getId())")
    @Mapping(target = "cityId", expression = "java(narrator.getCity().getId())")
    NarratorResponseDTO entityToDTO(Narrator narrator);

    List<NarratorResponseDTO> entityToDTO(Iterable<Narrator> narrators);

    Narrator dtoToEntity(NarratorRequestDTO narratorRequestDTO);

    List<Narrator> dtoToEntity(Iterable<NarratorRequestDTO> narratorDTOS);
}
