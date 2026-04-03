package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vacantes")
public class Vacante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String externalId;      // data-id de OCC — clave anti-duplicados

    private String titulo;
    private String empresa;
    private String ubicacion;
    private String salario;
    private String url;

    @Column(nullable = false)
    private LocalDateTime fechaEncontrada;

    @Column(nullable = false)
    private Boolean notificada = false;  // ¿ya se envió por Telegram?

    // Constructor vacío requerido por JPA
    public Vacante() {}

    // Constructor desde VacanteRaw
    public Vacante(String externalId, String titulo, String empresa,
                   String ubicacion, String salario, String url) {
        this.externalId      = externalId;
        this.titulo          = titulo;
        this.empresa         = empresa;
        this.ubicacion       = ubicacion;
        this.salario         = salario;
        this.url             = url;
        this.fechaEncontrada = LocalDateTime.now();
        this.notificada      = false;
    }

    // Getters
    public Long getId()                  { return id; }
    public String getExternalId()        { return externalId; }
    public String getTitulo()            { return titulo; }
    public String getEmpresa()           { return empresa; }
    public String getUbicacion()         { return ubicacion; }
    public String getSalario()           { return salario; }
    public String getUrl()               { return url; }
    public LocalDateTime getFechaEncontrada() { return fechaEncontrada; }
    public Boolean getNotificada()       { return notificada; }
    public void setNotificada(Boolean n) { this.notificada = n; }
}