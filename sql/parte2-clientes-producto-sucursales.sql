-- Clientes con algún producto inscrito que solo está disponible
-- en sucursales que ellos visitan.

SELECT DISTINCT c.nombre, c.apellidos
FROM Cliente c
INNER JOIN Inscripcion i ON i.idCliente = c.id
WHERE EXISTS (
        SELECT 1
        FROM Disponibilidad d_exists
        WHERE d_exists.idProducto = i.idProducto
    )
  AND NOT EXISTS (
        SELECT 1
        FROM Disponibilidad d
        WHERE d.idProducto = i.idProducto
          AND NOT EXISTS (
                SELECT 1
                FROM Visitan v
                WHERE v.idCliente = c.id
                  AND v.idSucursal = d.idSucursal
            )
    );
