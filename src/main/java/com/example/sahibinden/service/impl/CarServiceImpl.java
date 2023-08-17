package com.example.sahibinden.service.impl;

import com.example.sahibinden.exception.model.CustomException;
import com.example.sahibinden.model.Car;
import com.example.sahibinden.model.entity.*;
import com.example.sahibinden.repository.*;
import com.example.sahibinden.service.CarService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final MarkaRepository markaService;
    private final ModelRepository modelService;
    private final MotorRepository motorService;
    private final OzellikRepository ozellikService;
    private final PaketRepository paketService;
    private final KasaRepository kasaService;

    public Car getCarById(Long id) {
        CarEntity carEntity = carRepository.findById(id).orElseThrow();
        return Car.fromEntity(carEntity);
    }

    public List<Car> getAllCars() {
        List<CarEntity> carEntities = carRepository.findAll();
        return carEntities.stream()
                .map(Car::fromEntity)
                .collect(Collectors.toList());
    }

    public Car addCar(Car car) {
        MarkaEntity marka = markaService.findById(car.getMarka().getId()).get();
        ModelEntity model = modelService.findById(car.getModel().getId()).get();
        MotorEntity motor = motorService.findById(car.getMotor().getId()).get();
        OzellikEntity ozellik = ozellikService.findById(car.getModel().getId()).get();
        PaketEntity paket = paketService.findById(car.getPaket().getId()).get();
        KasaEntity kasa = kasaService.findById(car.getKasa().getId()).get();
        CarEntity carEntity = CarEntity.fromModel(car);
        carEntity.setMarka(marka);
        carEntity.setModel(model);
        carEntity.setMotor(motor);
        carEntity.setOzellik(ozellik);
        carEntity.setPaket(paket);
        carEntity.setKasa(kasa);
        CarEntity addedCarEntity = carRepository.save(carEntity);

        return Car.fromEntity(addedCarEntity);

    }

    public List<String> parseWebPage(String url) {
        List<String> parsedDataList = new ArrayList<>();

        try {
            Document document = Jsoup.connect(url).get();
            Element seriallist = document.getElementsByClass("seriallist").first();

            for (Element link : seriallist.children()) {
                String linkText = link.text();
                String linkHref = link.attr("href");
                parsedDataList.add("Text: " + linkText + ", URL: " + linkHref);
            }
        } catch (IOException e) {
            // Hata yönetimi burada yapılabilir
            e.printStackTrace();
        }

        return parsedDataList;
    }

    public Car updateCar(Car car) {
        if (carRepository.existsById(car.getId())) {
            CarEntity updatedCarEntity = carRepository.save(CarEntity.fromModel(car));
            return Car.fromEntity(updatedCarEntity);
        }
        throw new CustomException(HttpStatus.NOT_FOUND, "Girdiğiniz id bulunamadı: " + car.getId());
    }

    public boolean deleteCarById(Long id) {
        carRepository.deleteById(id);
        return !carRepository.existsById(id);


    }
}