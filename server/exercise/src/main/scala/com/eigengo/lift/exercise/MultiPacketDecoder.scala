package com.eigengo.lift.exercise

import java.nio.{ByteOrder, ByteBuffer}

import scodec.bits.{BitVector, ByteOrdering}

import scalaz.\/

/**
 * The multi packet message follows a simple structure
 *
 * {{{
 *    header: UInt16     = 0xcab0 // 2
 *    count: Byte        = ...    // 3
 *    ===
 *    size0: UInt16      = ...    // 5
 *    sloc0: Byte        = ...    // 6
 *    data0: Array[Byte] = ...
 *    size1: UInt16      = ...
 *    sloc1: Byte        = ...
 *    data1: Array[Byte] = ...
 *    ...
 *    sizen: UInt16      = ...
 *    slocn: Byte        = ...
 *    datan: Array[Byte] = ...
 * }}}
 */
object MultiPacketDecoder {
  private val header = 0xcab0.toShort

  def decodeShort(b0: Byte, b1: Byte): Int = {
    (b0 << 8) + b1
  }

  def decodeSensorDataSourceLocation(sloc: Byte): String \/ SensorDataSourceLocation = sloc match {
    case 0x01 ⇒ \/.right(SensorDataSourceLocationWrist)
    case 0x02 ⇒ \/.right(SensorDataSourceLocationWaist)
    case 0x03 ⇒ \/.right(SensorDataSourceLocationChest)
    case 0x04 ⇒ \/.right(SensorDataSourceLocationFoot)
    case 0xff ⇒ \/.right(SensorDataSourceLocationAny)
    case x    ⇒ \/.left(s"Unknown sensor data source location $x")
  }

  def decode(input: ByteBuffer): String \/ MultiPacket = {
    val bebb = input.order(ByteOrder.BIG_ENDIAN)
    if (input.limit() < 7) \/.left("No viable input: size < 7.")
    else {
      val inputHeader = input.getShort
      if (inputHeader != header) \/.left(s"Incorrect header. Expected $header, got $inputHeader.")
      else {
        val count = bebb.get()
        if (count == 0) \/.left("No content")
        else {
          var position = bebb.position()
          val (h :: t) = (0 until count).toList.map { _ ⇒
            val size = decodeShort(bebb.get, bebb.get)
            val sloc = decodeSensorDataSourceLocation(bebb.get())
            val buf = bebb.slice().limit(size).asInstanceOf[ByteBuffer]
            position = position + size
            bebb.position(position)

            sloc.map(sloc ⇒ PacketWithLocation(sloc, BitVector(buf)))
          }

          t.foldLeft(h.map(MultiPacket.apply))((r, b) ⇒ r.flatMap(mp ⇒ b.map(mp.withNewPacket)))
        }
      }
    }
  }

}
