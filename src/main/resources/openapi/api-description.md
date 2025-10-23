# 🎮 Pokemon Service API Documentation

This service provides access to Pokemon data from PokeAPI through both **SOAP** and **REST** interfaces.

---

## 🧼 SOAP Web Service

### Service Endpoints
- **🌐 WSDL URL**: `/pokemon/ws/pokemon.wsdl` ([Click here to view WSDL](/pokemon/ws/pokemon.wsdl))
- **📡 SOAP Endpoint**: `/pokemon/ws`
- **📦 Namespace**: `http://bankaya.com/pokemon/soap`
- **🔧 SOAP Version**: 1.1

### Available SOAP Operations

| Operation | Description | Input | Output |
|-----------|-------------|-------|--------|
| **GetPokemonAbilities** | Retrieves all abilities of a Pokemon | `<name>pikachu</name>` | List of abilities with details |
| **GetPokemonBaseExperience** | Get base experience points | `<name>pikachu</name>` | Integer value |
| **GetPokemonHeldItems** | Get items Pokemon can hold | `<name>pikachu</name>` | List of held items |
| **GetPokemonId** | Get Pokemon numeric ID | `<name>pikachu</name>` | Integer ID |
| **GetPokemonName** | Validate and return Pokemon name | `<name>pikachu</name>` | String name |
| **GetPokemonLocationAreaEncounters** | Get encounter locations | `<name>pikachu</name>` | URL to encounters |

### SOAP Request Example
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:pok="http://bankaya.com/pokemon/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <pok:GetPokemonAbilitiesRequest>
         <pok:name>pikachu</pok:name>
      </pok:GetPokemonAbilitiesRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

### SOAP Response Example
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Body>
      <ns2:GetPokemonAbilitiesResponse xmlns:ns2="http://bankaya.com/pokemon/soap">
         <ns2:abilities>
            <ns2:name>static</ns2:name>
            <ns2:url>https://pokeapi.co/api/v2/ability/9/</ns2:url>
            <ns2:isHidden>false</ns2:isHidden>
            <ns2:slot>1</ns2:slot>
         </ns2:abilities>
         <ns2:abilities>
            <ns2:name>lightning-rod</ns2:name>
            <ns2:url>https://pokeapi.co/api/v2/ability/31/</ns2:url>
            <ns2:isHidden>true</ns2:isHidden>
            <ns2:slot>3</ns2:slot>
         </ns2:abilities>
      </ns2:GetPokemonAbilitiesResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

### SOAP Fault Examples

**CLIENT Fault (400 - Bad Request)**
```xml
<SOAP-ENV:Fault>
    <faultcode>SOAP-ENV:Client</faultcode>
    <faultstring>Pokemon name cannot be null or empty</faultstring>
</SOAP-ENV:Fault>
```

**SERVER Fault (404 - Not Found)**
```xml
<SOAP-ENV:Fault>
    <faultcode>SOAP-ENV:Server</faultcode>
    <faultstring>Pokemon with name 'invalidpokemon' not found</faultstring>
</SOAP-ENV:Fault>
```

### Testing SOAP Service

**Using cURL:**
```bash
curl -X POST http://localhost:8080/pokemon/ws \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:pok="http://bankaya.com/pokemon/soap">
        <soapenv:Body>
          <pok:GetPokemonIdRequest>
            <pok:name>pikachu</pok:name>
          </pok:GetPokemonIdRequest>
        </soapenv:Body>
      </soapenv:Envelope>'
```

**Using SoapUI:**
1. Import WSDL from: `http://localhost:8080/pokemon/ws/pokemon.wsdl`
2. Generate requests for any operation
3. Execute and view responses

---

## 🌐 REST API

This service provides **two types of REST endpoints**:

### 1. Direct Pokemon API (`/pokemon/{name}`)
- Direct access to complete Pokemon data
- Returns full Pokemon object with all attributes
- Simpler integration for getting all Pokemon information at once

### 2. SOAP Proxy API (`/api/soap/pokemon/{name}/*`)
These endpoints **proxy SOAP operations** allowing you to test SOAP functionality through REST:

| Endpoint | SOAP Operation | Description |
|----------|---------------|-------------|
| `GET /api/soap/pokemon/{name}/abilities` | GetPokemonAbilities | Get Pokemon abilities |
| `GET /api/soap/pokemon/{name}/base-experience` | GetPokemonBaseExperience | Get base experience |
| `GET /api/soap/pokemon/{name}/held-items` | GetPokemonHeldItems | Get held items |
| `GET /api/soap/pokemon/{name}/id` | GetPokemonId | Get Pokemon ID |
| `GET /api/soap/pokemon/{name}/name` | GetPokemonName | Validate Pokemon name |
| `GET /api/soap/pokemon/{name}/locations` | GetPokemonLocationAreaEncounters | Get location encounters |

**✨ Use "Try it out" in Swagger UI to test SOAP operations without SOAP client!**

---

## ✨ Features

- ⚡ **Caching**: Caffeine cache for optimized performance
- 📊 **Audit Logging**: All SOAP requests logged to database
- 🔒 **Error Handling**: Proper SOAP Faults and HTTP status codes
- 🔄 **PokeAPI Integration**: Real-time data from PokeAPI
- 🏗️ **Hexagonal Architecture**: Clean, maintainable code structure

---

## 📚 Additional Resources

- [PokeAPI Documentation](https://pokeapi.co/docs/v2)
- [SOAP 1.1 Specification](https://www.w3.org/TR/2000/NOTE-SOAP-20000508/)
- [Download WSDL](/pokemon/ws/pokemon.wsdl)
