package com.fregence.fregence.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fregence.fregence.dto.PerfumeDTO;
import com.fregence.fregence.entity.Perfume;
import com.fregence.fregence.entity.Gender;
import com.fregence.fregence.repository.PerfumeRepository;

@ExtendWith(MockitoExtension.class) // Mockito-nu aktiv edirik
public class PerfumeServiceTest {

    @Mock
    private PerfumeRepository repository; // Repository-ni saxtalaşdırırıq (Mock)

    @Mock // <--- BU SƏTRİ VƏ ALTDAKI DƏYİŞƏNİ ƏLAVƏ ET
    private FileService fileService;
    
    @InjectMocks
    private PerfumeService service; // Mock olunmuş repository-ni bura daxil et

    private Perfume samplePerfume;

    @BeforeEach
    void setUp() {
        // Hər testdən əvvəl bir nümunə ətir obyekti hazırlayırıq
        samplePerfume = new Perfume();
        samplePerfume.setId(1L);
        samplePerfume.setName("Sauvage");
        samplePerfume.setBrand("Dior");
        samplePerfume.setPrice(200.0);
        samplePerfume.setGender(Gender.MEN);
        samplePerfume.setIsNew(true);
    }

    @Test
    void getPerfumeById_UgurlaTapilanda() {
        // GIVEN (Verilənlər)
        // Repository-yə deyirik ki, 1 nömrəli ID istənilsə, bizim hazırladığımız obyekti qaytar
        when(repository.findById(1L)).thenReturn(Optional.of(samplePerfume));

        // WHEN (İcra ediləndə)
        PerfumeDTO result = service.getPerfumeById(1L);

        // THEN (Nəticənin yoxlanılması)
        assertNotNull(result);
        assertEquals("Sauvage", result.getName());
        assertEquals("Dior", result.getBrand());
        
        // Repository-nin həqiqətən 1 dəfə çağırıldığını yoxlayırıq
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void getPerfumeById_TapilmayandaXetaAtmali() {
        // GIVEN
        // 99 nömrəli ID üçün boş nəticə qaytar
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN (İcra et və xətanı gözlə)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getPerfumeById(99L);
        });

        assertEquals("Perfume not found with id: 99", exception.getMessage());
    }

    @Test
    void deletePerfume_UgurlaSilinmeli() {
        // GIVEN
        when(repository.findById(1L)).thenReturn(Optional.of(samplePerfume));

        // WHEN
        service.deletePerfume(1L);

        // THEN
        // Repository-nin delete metodunun çağırıldığını yoxlayırıq
        verify(repository, times(1)).delete(samplePerfume);
    }
}