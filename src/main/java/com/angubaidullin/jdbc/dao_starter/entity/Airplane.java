package com.angubaidullin.jdbc.dao_starter.entity;

import java.util.Arrays;
import java.util.Objects;

public class Airplane {
    private Long id;
    private String model;
    private byte[] image;

    public Airplane() {
    }

    public Airplane(String model, byte[] image) {
        this.model = model;
        this.image = image;
    }

    public Airplane(Long id, String model, byte[] image) {
        this.id = id;
        this.model = model;
        this.image = image;
    }

    public Airplane(Long id, String model) {
        this.id = id;
        this.model = model;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airplane airplane = (Airplane) o;
        return Objects.equals(id, airplane.id) && Objects.equals(model, airplane.model) && Objects.deepEquals(image, airplane.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, model, Arrays.hashCode(image));
    }

    @Override
    public String toString() {
        return "Airplane{" +
                "id=" + id +
                ", model='" + model + '\'' +
                ", image=" + Arrays.toString(image) +
                '}';
    }
}
