$basePath = "C:\Users\sierb\Documents\Universidad\Desarrollo_Empresarial\proyectos\Taller_Semana_7\loan-api\1. Equipos"

# FILE 1: Crear equipo - camara.request.yaml
$content1 = '$kind: http-request
description: ""
url: ''{{baseUrl}}/equipment''
method: POST
headers:
  - key: Content-Type
    value: application/json
body:
  type: json
  content: |-
    {
      "code": "CAM-001",
      "name": "Camara Canon EOS",
      "stock": 2
    }
scripts:
  - type: afterResponse
    code: |-
      pm.test("Responde 201 - Creado", function () {
        pm.response.to.have.status(201);
      });

      const json = pm.response.json();

      pm.test("Tiene id generado", function () {
        pm.expect(json.id).to.be.a("string").and.not.empty;
      });

      pm.test("El codigo es CAM-001", function () {
        pm.expect(json.code).to.eql("CAM-001");
      });

      pm.test("El nombre es Camara Canon EOS", function () {
        pm.expect(json.name).to.eql("Camara Canon EOS");
      });

      pm.test("El stock inicial es 2", function () {
        pm.expect(json.stock).to.eql(2);
      });

      pm.test("Content-Type es application/json", function () {
        pm.response.to.have.header("Content-Type");
        pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
      });

      pm.collectionVariables.set("camaraId", json.id);
    language: text/javascript
order: 2000'

[System.IO.File]::WriteAllText((Join-Path $basePath "Crear equipo - camara.request.yaml"), $content1, [System.Text.Encoding]::UTF8)
Write-Host "File 1 written"

# FILE 2: Crear equipo - portatil.request.yaml
$content2 = '$kind: http-request
description: ""
url: ''{{baseUrl}}/equipment''
method: POST
headers:
  - key: Content-Type
    value: application/json
body:
  type: json
  content: |-
    {
      "code": "PORT-001",
      "name": "Portatil Lenovo T14",
      "stock": 5
    }
scripts:
  - type: afterResponse
    code: |-
      pm.test("Responde 201 - Creado", function () {
        pm.response.to.have.status(201);
      });

      const json = pm.response.json();

      pm.test("Tiene id generado", function () {
        pm.expect(json.id).to.be.a("string").and.not.empty;
      });

      pm.test("El codigo es PORT-001", function () {
        pm.expect(json.code).to.eql("PORT-001");
      });

      pm.test("El nombre contiene Portatil", function () {
        pm.expect(json.name).to.include("Portatil");
      });

      pm.test("El stock inicial es 5", function () {
        pm.expect(json.stock).to.eql(5);
      });

      pm.test("Content-Type es application/json", function () {
        pm.response.to.have.header("Content-Type");
        pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
      });

      pm.collectionVariables.set("portatilId", json.id);
    language: text/javascript
order: 1000'

[System.IO.File]::WriteAllText((Join-Path $basePath "Crear equipo - portatil.request.yaml"), $content2, [System.Text.Encoding]::UTF8)
Write-Host "File 2 written"

# FILE 3: Crear equipo - sensor.request.yaml
$content3 = '$kind: http-request
description: ""
url: ''{{baseUrl}}/equipment''
method: POST
headers:
  - key: Content-Type
    value: application/json
body:
  type: json
  content: |-
    {
      "code": "SEN-001",
      "name": "Sensor Arduino DHT22",
      "stock": 10
    }
scripts:
  - type: afterResponse
    code: |-
      pm.test("Responde 201 - Creado", function () {
        pm.response.to.have.status(201);
      });

      const json = pm.response.json();

      pm.test("Tiene id generado", function () {
        pm.expect(json.id).to.be.a("string").and.not.empty;
      });

      pm.test("El codigo es SEN-001", function () {
        pm.expect(json.code).to.eql("SEN-001");
      });

      pm.test("El nombre contiene Sensor", function () {
        pm.expect(json.name).to.include("Sensor");
      });

      pm.test("El stock es un numero positivo", function () {
        pm.expect(json.stock).to.be.a("number").and.above(0);
      });

      pm.test("Content-Type es application/json", function () {
        pm.response.to.have.header("Content-Type");
        pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
      });

      pm.collectionVariables.set("sensorId", json.id);
    language: text/javascript
order: 3000'

[System.IO.File]::WriteAllText((Join-Path $basePath "Crear equipo - sensor.request.yaml"), $content3, [System.Text.Encoding]::UTF8)
Write-Host "File 3 written"

# FILE 4: Listar equipos.request.yaml
$content4 = '$kind: http-request
description: ""
url: ''{{baseUrl}}/equipment''
method: GET
scripts:
  - type: afterResponse
    code: |-
      pm.test("Responde 200 - OK", function () {
        pm.response.to.have.status(200);
      });

      const json = pm.response.json();

      pm.test("La respuesta es un arreglo", function () {
        pm.expect(json).to.be.an("array");
      });

      pm.test("Hay al menos 3 equipos creados", function () {
        pm.expect(json.length).to.be.at.least(3);
      });

      pm.test("Cada equipo tiene id, code, name y stock", function () {
        json.forEach(function (equipo) {
          pm.expect(equipo).to.have.property("id");
          pm.expect(equipo).to.have.property("code");
          pm.expect(equipo).to.have.property("name");
          pm.expect(equipo).to.have.property("stock");
        });
      });

      pm.test("Content-Type es application/json", function () {
        pm.response.to.have.header("Content-Type");
        pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
      });
    language: text/javascript
order: 4000'

[System.IO.File]::WriteAllText((Join-Path $basePath "Listar equipos.request.yaml"), $content4, [System.Text.Encoding]::UTF8)
Write-Host "File 4 written"

# FILE 5: Obtener equipo por id.request.yaml
$content5 = '$kind: http-request
description: ""
url: ''{{baseUrl}}/equipment/{{portatilId}}''
method: GET
scripts:
  - type: afterResponse
    code: |-
      pm.test("Responde 200 - OK", function () {
        pm.response.to.have.status(200);
      });

      const json = pm.response.json();

      pm.test("El id coincide con portatilId", function () {
        pm.expect(json.id).to.eql(pm.collectionVariables.get("portatilId"));
      });

      pm.test("Tiene campo code", function () {
        pm.expect(json.code).to.be.a("string").and.not.empty;
      });

      pm.test("Tiene campo name", function () {
        pm.expect(json.name).to.be.a("string").and.not.empty;
      });

      pm.test("El stock es un numero no negativo", function () {
        pm.expect(json.stock).to.be.a("number").and.at.least(0);
      });

      pm.test("Content-Type es application/json", function () {
        pm.response.to.have.header("Content-Type");
        pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
      });
    language: text/javascript
order: 5000'

[System.IO.File]::WriteAllText((Join-Path $basePath "Obtener equipo por id.request.yaml"), $content5, [System.Text.Encoding]::UTF8)
Write-Host "File 5 written"

# FILE 6: Actualizar equipo.request.yaml
$content6 = '$kind: http-request
description: ""
url: ''{{baseUrl}}/equipment/{{portatilId}}''
method: PUT
headers:
  - key: Content-Type
    value: application/json
body:
  type: json
  content: |-
    {
      "code": "PORT-001",
      "name": "Portatil Lenovo T14",
      "stock": 6
    }
scripts:
  - type: afterResponse
    code: |-
      pm.test("Responde 200 - OK", function () {
        pm.response.to.have.status(200);
      });

      const json = pm.response.json();

      pm.test("El id sigue siendo portatilId", function () {
        pm.expect(json.id).to.eql(pm.collectionVariables.get("portatilId"));
      });

      pm.test("El codigo fue actualizado a PORT-001", function () {
        pm.expect(json.code).to.eql("PORT-001");
      });

      pm.test("El nombre fue actualizado", function () {
        pm.expect(json.name).to.eql("Portatil Lenovo T14");
      });

      pm.test("El stock fue actualizado a 6", function () {
        pm.expect(json.stock).to.eql(6);
      });

      pm.test("Content-Type es application/json", function () {
        pm.response.to.have.header("Content-Type");
        pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
      });
    language: text/javascript
order: 6000'

[System.IO.File]::WriteAllText((Join-Path $basePath "Actualizar equipo.request.yaml"), $content6, [System.Text.Encoding]::UTF8)
Write-Host "File 6 written"

Write-Host "All 6 files written successfully!"
