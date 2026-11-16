package ar.edu.up.tap.logistica.web;

import ar.edu.up.tap.logistica.servicio.CatalogoService;
import ar.edu.up.tap.logistica.servicio.StockService;
import ar.edu.up.tap.logistica.web.dto.CrearDestinatarioRequest;
import ar.edu.up.tap.logistica.web.dto.DepositoResponse;
import ar.edu.up.tap.logistica.web.dto.DestinatarioResponse;
import ar.edu.up.tap.logistica.web.dto.PaginaResponse;
import ar.edu.up.tap.logistica.web.dto.ProductoResponse;
import ar.edu.up.tap.logistica.web.dto.StockResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogoService;
    private final StockService stockService;

    public CatalogoController(CatalogoService catalogoService, StockService stockService) {
        this.catalogoService = catalogoService;
        this.stockService = stockService;
    }

    @GetMapping("/depositos")
    public PaginaResponse<DepositoResponse> listarDepositos(@PageableDefault(sort = "id") Pageable pageable) {
        return catalogoService.listarDepositos(pageable);
    }

    @GetMapping("/depositos/{id}/stock")
    public PaginaResponse<StockResponse> stock(@PathVariable Long id, @PageableDefault(sort = "id") Pageable pageable) {
        return stockService.listar(id, pageable);
    }

    @GetMapping("/productos")
    public PaginaResponse<ProductoResponse> listarProductos(@PageableDefault(sort = "nombre") Pageable pageable) {
        return catalogoService.listarProductos(pageable);
    }

    @GetMapping("/destinatarios")
    public PaginaResponse<DestinatarioResponse> listarDestinatarios(@PageableDefault(sort = "nombre") Pageable pageable) {
        return catalogoService.listarDestinatarios(pageable);
    }

    @PostMapping("/destinatarios")
    public ResponseEntity<DestinatarioResponse> crearDestinatario(@Valid @RequestBody CrearDestinatarioRequest request) {
        DestinatarioResponse creado = catalogoService.crearDestinatario(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/destinatarios/" + creado.id()))
                .body(creado);
    }
}
