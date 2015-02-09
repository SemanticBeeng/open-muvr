package com.eigengo.lift.exercise.classifiers.workflows

import com.eigengo.lift.exercise._

object ClassificationAssertions {

  /**
   * Facts that may hold of sensor data. Facts are presented in positive/negative pairs. This allows us to keep
   * assertions in negation normal form (NNF).
   */
  trait Fact
  case object True extends Fact
  case object False extends Fact
  /**
   * Named gesture matches with probability >= `matchProbability`
   */
  case class Gesture(name: String, matchProbability: Double) extends Fact
  /**
   * Named gesture matches with probability < `matchProbability`
   */
  case class NegGesture(name: String, matchProbability: Double) extends Fact

  /**
   * Convenience function that provides negation on facts, whilst keeping them in NNF. Translation is linear in the
   * size of the fact.
   */
  def not(fact: Fact): Fact = fact match {
    case True =>
      False

    case False =>
      True

    case Gesture(name, probability) =>
      NegGesture(name, probability)

    case NegGesture(name, probability) =>
      Gesture(name, probability)
  }

  /**
   * Bind inferred (e.g. machine learnt) assertions to sensors in a network of sensorse.
   *
   * @param wrist   facts true of this location
   * @param waist   facts true of this location
   * @param foot    facts true of this location
   * @param chest   facts true of this location
   * @param unknown facts true of this location
   * @param value   raw sensor network data that assertion holds for
   */
  case class BindToSensors[WR <: SensorValue, WA <: SensorValue, FO <: SensorValue, CH <: SensorValue, UN <: SensorValue](wrist: Set[Fact], waist: Set[Fact], foot: Set[Fact], chest: Set[Fact], unknown: Set[Fact], value: SensorNet[WR, WA, FO, CH, UN]) {
    val toMap = Map[SensorDataSourceLocation, Set[Fact]](
      SensorDataSourceLocationWrist -> wrist,
      SensorDataSourceLocationWaist -> waist,
      SensorDataSourceLocationFoot -> foot,
      SensorDataSourceLocationChest -> chest,
      SensorDataSourceLocationAny -> unknown
    )
  }

}
