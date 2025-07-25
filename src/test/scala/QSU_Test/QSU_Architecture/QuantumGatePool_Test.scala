package QSU_Test.QSU_Architecture

import QuantumStateUnit.Gates.QGPMuxLayer
import QuantumStateUnit.QSU_Architecture._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

//QGPTest 1: NonFPU GatePool
class QGPMuxLayerCheck extends AnyFlatSpec with ChiselScalatestTester {
  "MuxLayer" should "SwitchBetweenVectors" in
    test(new QGPMuxLayer(3,3,3)) { dut =>
      //input of no-op, pauliX, and a reverse order
      for (i <- 0 until 8) {
        dut.io.in_QSV(0)(i).poke(i.U)
        dut.io.in_QSV(2)(i).poke((7 - i).U)
      }
      for (i <- 0 until 4) {
        dut.io.in_QSV(1)(i).poke((i + 1).U)
        dut.io.in_QSV(1)(i + 1).poke(i.U)
      }
      dut.io.in_sel.poke(0.U)
      dut.clock.step()
      for (i <- 0 until 8) {
        dut.io.out_QSV(i).expect(i.U)
      }
      dut.clock.step()
      dut.io.in_sel.poke(2.U)
      dut.clock.step()
      for (i <- 0 until 8) {
        dut.io.out_QSV(i).expect((7 - i).U)
      }
    }
}


//QGPTest 1: NonFPU GatePool
class QGPTest1 extends AnyFlatSpec with ChiselScalatestTester {
  "Gates" should "GenerateNewState" in
    test(new QGP(3,32,3,3,10)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      //Initial Input
      dut.io.in_QSV(0).poke("h3c000000".U) //000: 1 + 0j
      dut.io.in_QSV(1).poke("h3c000000".U) //001: 1 + 0j
      dut.io.in_QSV(2).poke("h3c000000".U) //010: 1 + 0j
      dut.io.in_QSV(3).poke("h00003c00".U) //011: 0 + 1j
      dut.io.in_QSV(4).poke("h00003c00".U) //100: 0 + 1j
      dut.io.in_QSV(5).poke("h00003c00".U) //101: 0 + 1j
      dut.io.in_QSV(6).poke("h3c004000".U) //110: 1 + 2j
      dut.io.in_QSV(7).poke("h42004400".U) //111: 3 + 4j
      dut.io.in_valid.poke(0.B)

      //Test Pauli_X
      dut.clock.step()
      dut.io.in_sel.poke("h01".U)
      dut.io.in_valid.poke(1.B)

      dut.clock.step()
      dut.io.out_QSV(0).expect("h3c000000".U) //000: 1 + 0j
      dut.io.out_QSV(1).expect("h3c000000".U) //001: 1 + 0j
      dut.io.out_QSV(2).expect("h00003c00".U) //010: 0 + 1j
      dut.io.out_QSV(3).expect("h3c000000".U) //011: 1 + 0j
      dut.io.out_QSV(4).expect("h00003c00".U) //100: 0 + 1j
      dut.io.out_QSV(5).expect("h00003c00".U) //101: 0 + 1j
      dut.io.out_QSV(6).expect("h42004400".U) //110: 3 + 4j
      dut.io.out_QSV(7).expect("h3c004000".U) //111: 1 + 2j
      dut.io.out_valid.expect(1.B)
      dut.io.in_valid.poke(0.B)

      //Test Pauli_Y
      dut.clock.step()
      dut.io.in_sel.poke("h02".U)
      dut.io.in_valid.poke(1.B)

      dut.clock.step()
      dut.io.out_QSV(0).expect("h00003c00".U) //000: 0 + 1j
      dut.io.out_QSV(1).expect("h0000bc00".U) //001: 0 - 1j
      dut.io.out_QSV(2).expect("hbc000000".U) //010:-1 + 0j
      dut.io.out_QSV(3).expect("h0000bc00".U) //011: 0 - 1j
      dut.io.out_QSV(4).expect("hbc000000".U) //100:-1 + 0j
      dut.io.out_QSV(5).expect("h3c000000".U) //101: 1 + 0j
      dut.io.out_QSV(6).expect("hc4004200".U) //110:-4 + 3j
      dut.io.out_QSV(7).expect("h4000bc00".U) //111: 2 - 1j
      dut.io.out_valid.expect(1.B)
      dut.io.in_valid.poke(0.B)

      //Test CNot
        dut.clock.step()
        dut.io.in_sel.poke("h06".U)
        dut.io.in_valid.poke(1.B)

        dut.clock.step()
        dut.io.out_QSV(0).expect("h3c000000".U) //000: 1 + 0j
        dut.io.out_QSV(1).expect("h3c000000".U) //001: 1 + 0j
        dut.io.out_QSV(2).expect("h00003c00".U) //010: 0 + 1j
        dut.io.out_QSV(3).expect("h3c000000".U) //011: 1 + 0j
        dut.io.out_QSV(4).expect("h00003c00".U) //100: 0 + 1j
        dut.io.out_QSV(5).expect("h00003c00".U) //101: 0 + 1j
        dut.io.out_QSV(6).expect("h42004400".U) //110: 3 + 4j
        dut.io.out_QSV(7).expect("h3c004000".U) //111: 1 + 2j
        dut.io.out_valid.expect(1.B)
    }
}

//FPU Test
class QGPTest2 extends AnyFlatSpec with ChiselScalatestTester {
  "Gates" should "multiply" in
    test(new QGP(3, 32, 3, 3,10)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      //Initial Input
      dut.io.in_QSV(0).poke("h3c000000".U) //000:  1  +   0j
      dut.io.in_QSV(1).poke("h00000000".U) //001:  0  +   0j
      dut.io.in_QSV(2).poke("h39A80000".U) //010: 0.7 +   0j
      dut.io.in_QSV(3).poke("h0000B9A8".U) //011:  0  - 0.7j
      dut.io.in_QSV(4).poke("h39A80000".U) //100: 0.7 +   0j
      dut.io.in_QSV(5).poke("h39A80000".U) //101: 0.7 +   0j
      dut.io.in_QSV(6).poke("h00000000".U) //110:  0  +   0j
      dut.io.in_QSV(7).poke("h3c000000".U) //111:  1  +   0j
      dut.io.in_valid.poke(0.B)
      dut.io.in_sel.poke("h00".U)

      //UGate input, will be 0 and remove everything
      dut.io.in_Ugate(0).poke(0.U)
      dut.io.in_Ugate(1).poke(0.U)
      dut.io.in_Ugate(2).poke(0.U)
      dut.io.in_Ugate(3).poke(0.U)

      //Hadamard
      dut.clock.step(2)
      dut.io.in_sel.poke("h11".U)
      dut.io.in_valid.poke(1.B)
      dut.clock.step(30)
      dut.io.in_valid.poke(0.B)

      //T gate
      dut.clock.step()
      dut.io.in_sel.poke("h14".U)
      dut.io.in_valid.poke(1.B)
      dut.clock.step(30)
      dut.io.in_valid.poke(0.B)

      //U gate
      dut.clock.step()
      dut.io.in_sel.poke("h16".U)
      dut.io.in_valid.poke(1.B)
      dut.clock.step(30)
      dut.io.in_valid.poke(0.B)
    }
}

//Measure gate test: select is incorrect with out QSV
class QGPTest3 extends AnyFlatSpec with ChiselScalatestTester {
  "Gates" should "measureQ0" in
    test(new QGP(3, 32, 3, 3, 10)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      //Initial Input
      dut.io.in_QSV(0).poke("h38000000".U) //000: 0.50
      dut.io.in_QSV(1).poke("h34000000".U) //001: 0.25
      dut.io.in_QSV(2).poke("h00000000".U) //010: 0.00
      dut.io.in_QSV(3).poke("h34000000".U) //011: 0.25
      dut.io.in_QSV(4).poke("h38000000".U) //100: 0.50
      dut.io.in_QSV(5).poke("h34000000".U) //101: 0.25
      dut.io.in_QSV(6).poke("h38000000".U) //110: 0.50
      dut.io.in_QSV(7).poke("h34000000".U) //111: 0.25
      dut.io.in_valid.poke(0.B)
      dut.io.in_sel.poke("h00".U)
      dut.io.in_noise.poke("h34874298".U)

      //controller says the input is valid to modify
      dut.clock.step()
      dut.io.in_valid.poke(1.B)
      dut.io.in_sel.poke(31.U) //Measure 0th qubit

      //Let gate pool to do math
      dut.clock.step(80)

      //Valid
      dut.io.out_valid.expect(1.B)

      //Output
      val peekMQ  = dut.io.out_MQ.peek().litValue
      println(s"|000> : ${dut.io.out_QSV(0).peek().litValue.toString(16)}")
      println(s"|001> : ${dut.io.out_QSV(1).peek().litValue.toString(16)}")
      println(s"|010> : ${dut.io.out_QSV(2).peek().litValue.toString(16)}")
      println(s"|011> : ${dut.io.out_QSV(3).peek().litValue.toString(16)}")
      println(s"|100> : ${dut.io.out_QSV(4).peek().litValue.toString(16)}")
      println(s"|101> : ${dut.io.out_QSV(5).peek().litValue.toString(16)}")
      println(s"|110> : ${dut.io.out_QSV(6).peek().litValue.toString(16)}")
      println(s"|111> : ${dut.io.out_QSV(7).peek().litValue.toString(16)}")
      println(s"Measured Qubit Value: ${dut.io.out_MQ.peek().litValue.toString(1)}")

      //Test normalizing unnormalized states

      //in_valid is 0 and then select changes
      dut.clock.step()
      dut.io.in_valid.poke(0.B)
      dut.clock.step(30)

      //changes to new unnormalized value
      dut.io.in_QSV(0).poke("h00000000".U) //000: 0.00
      dut.io.in_QSV(1).poke("h34000000".U) //001: 0.25
      dut.io.in_QSV(2).poke("h00000000".U) //010: 0.00
      dut.io.in_QSV(3).poke("h34000000".U) //011: 0.25
      dut.io.in_QSV(4).poke("h00000000".U) //100: 0.00
      dut.io.in_QSV(5).poke("h34000000".U) //101: 0.25
      dut.io.in_QSV(6).poke("h00000000".U) //110: 0.00
      dut.io.in_QSV(7).poke("h34000000".U) //111: 0.25
      dut.io.in_valid.poke(1.B)
      dut.io.in_sel.poke("h10".U)

      //skip time
      dut.clock.step(80)
      println(s"|000> : ${dut.io.out_QSV(0).peek().litValue.toString(16)}")
      println(s"|001> : ${dut.io.out_QSV(1).peek().litValue.toString(16)}")
      println(s"|010> : ${dut.io.out_QSV(2).peek().litValue.toString(16)}")
      println(s"|011> : ${dut.io.out_QSV(3).peek().litValue.toString(16)}")
      println(s"|100> : ${dut.io.out_QSV(4).peek().litValue.toString(16)}")
      println(s"|101> : ${dut.io.out_QSV(5).peek().litValue.toString(16)}")
      println(s"|110> : ${dut.io.out_QSV(6).peek().litValue.toString(16)}")
      println(s"|111> : ${dut.io.out_QSV(7).peek().litValue.toString(16)}")
    }
}