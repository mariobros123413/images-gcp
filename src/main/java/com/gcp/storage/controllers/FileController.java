package com.gcp.storage.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.gcp.storage.services.GoogleCloudStorageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

	private final GoogleCloudStorageService storageService;

	public FileController(GoogleCloudStorageService storageService) {
		this.storageService = storageService;
	}

	@PostMapping("/upload")
	public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files,
			@RequestParam("path") String path) {
		List<String> fileUrls = new ArrayList<>();

		try {
			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					String fileUrl = storageService.uploadFile(file, path);
					fileUrls.add(fileUrl);
				}
			}
			return ResponseEntity.ok(fileUrls);
		} catch (IOException e) {
			return ResponseEntity.status(500).body(null);
		}
	}

	@DeleteMapping("/deleteFolder")
	public ResponseEntity<String> deleteFolder(@RequestParam("id_usuario") String id_usuario) {
		String folderPath = "gallery/" + id_usuario + "/"; // Ruta de la carpeta del usuario
		try {
			
			storageService.deleteFolder(folderPath);
			return ResponseEntity.ok("Carpeta y archivos eliminados exitosamente.");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error al eliminar la carpeta: " + e.getMessage());
		}
	}

	@GetMapping("/getFotos")
	public ResponseEntity<List<String>> getFotosUser(@RequestParam("id_usuario") String id_usuario) {
		try {
			// Generar el path de la carpeta del usuario
			String userFolderPath = "gallery/" + id_usuario + "/";

			// Llamar al servicio de almacenamiento para obtener las URLs de las imágenes
			List<String> imageUrls = storageService.getImagesFromFolder(userFolderPath);

			// Verificar si se encontraron imágenes
			if (imageUrls.isEmpty()) {
				return ResponseEntity.status(404).body(new ArrayList<>());
			} else {
				return ResponseEntity.ok(imageUrls);
			}
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ArrayList<>());
		}
	}

}
