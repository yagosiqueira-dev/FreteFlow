package br.com.freteflow.service;

import br.com.freteflow.dto.vehicle.VehicleRequestDTO;
import br.com.freteflow.dto.vehicle.VehicleResponseDTO;
import br.com.freteflow.entity.Vehicle;
import br.com.freteflow.exception.LicensePlateAlreadyExistsException;
import br.com.freteflow.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void shouldCreateVehicleWithNormalizedPlate() {
        VehicleRequestDTO request = new VehicleRequestDTO("abc1d23", "Truck", "Volvo FH", 2022);

        when(vehicleRepository.existsByLicensePlate("ABC1D23")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponseDTO response = vehicleService.createVehicle(request);

        assertThat(response.licensePlate()).isEqualTo("ABC1D23");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenLicensePlateAlreadyExists() {
        VehicleRequestDTO request = new VehicleRequestDTO("ABC1D23", "Truck", "Volvo FH", 2022);

        when(vehicleRepository.existsByLicensePlate("ABC1D23")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(LicensePlateAlreadyExistsException.class);

        verify(vehicleRepository, never()).save(any());
    }
}