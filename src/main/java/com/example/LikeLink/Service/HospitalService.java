package com.example.LikeLink.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.LikeLink.Exception.ResourceNotFoundException;
import com.example.LikeLink.Model.Hospital;
import com.example.LikeLink.Model.IncomingPatient;
import com.example.LikeLink.Repository.HospitalRepository;
import com.example.LikeLink.Repository.IncomingPatientRepository;
import com.example.LikeLink.dto.request.HospitalRegistrationRequest;
import java.util.List;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private IncomingPatientRepository incomingPatientRepository;

    @Transactional
    public Hospital registerHospital(String userId, HospitalRegistrationRequest request) {
        // Check if hospital already exists for this user
        if (hospitalRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Hospital already registered for this user");
        }

        Hospital hospital = new Hospital();
        hospital.setUserId(userId);
        hospital.setHospitalName(request.getHospitalName());
        hospital.setHospitalType(request.getHospitalType());
        hospital.setLicenseNumber(request.getLicenseNumber());
        hospital.setYearEstablished(request.getYearEstablished());
        hospital.setPhoneNumber(request.getPhoneNumber());
        hospital.setAddress(request.getAddress());
        hospital.setCity(request.getCity());
        hospital.setState(request.getState());
        hospital.setPinCode(request.getPinCode());
        hospital.setLatitude(request.getLatitude());
        hospital.setLongitude(request.getLongitude());
        hospital.setTotalBeds(request.getTotalBeds());
        hospital.setIcuBeds(request.getIcuBeds());
        hospital.setEmergencyBeds(request.getEmergencyBeds());
        hospital.setHasAmbulanceService(request.isHasAmbulanceService());
        hospital.setHasEmergencyService(request.isHasEmergencyService());
        hospital.setDepartments(request.getDepartments());

        return hospitalRepository.save(hospital);
    }

    public Hospital getHospitalById(String hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException(hospitalId));
    }

    public List<IncomingPatient> getIncomingPatients(String hospitalId) {
        // Verify hospital exists
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException(hospitalId);
        }

        return incomingPatientRepository.findByHospitalIdAndStatus(
            hospitalId, 
            IncomingPatient.Status.PENDING
        );
    }
}