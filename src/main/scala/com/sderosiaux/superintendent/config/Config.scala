package com.sderosiaux.superintendent.config

import com.sderosiaux.superintendent.connectors.Connector
import com.sderosiaux.superintendent.format.Format
import com.sderosiaux.superintendent.mapping.Mapping

case class SuperIntendent(
  formats: List[Format[_]],
  connectors: List[Connector[_]],
  mappings: List[Mapping[_, _]]
)
