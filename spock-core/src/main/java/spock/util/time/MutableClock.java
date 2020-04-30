package spock.util.time;

import java.time.*;
import java.time.temporal.*;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A mutable implementation of {@link Clock} for testing purposes.
 *
 * @author Leonard Brünings
 * @since 2.0
 */
public class MutableClock extends Clock {

  private final ZoneId zone;
  private Instant instant;

  /**
   * {@link TemporalAmount} used for {@link #next()} and {@link #previous()} changes.
   */
  private TemporalAmount changeAmount;

  /**
   * Initializes a with {@link Instant#now()} and {@link ZoneId#systemDefault()} and 1 second {@link #changeAmount}.
   */
  public MutableClock() {
    this(Instant.now(), ZoneId.systemDefault());
  }

  /**
   * Initializes a with given instant and {@link ZoneId#systemDefault()} and 1 second {@link #changeAmount}.
   * @param instant to use
   */
  public MutableClock(Instant instant) {
    this(instant, ZoneId.systemDefault());
  }

  /**
   * Initializes a with {@link Instant#now()} and given zone and 1 second {@link #changeAmount}.
   * @param zone to use
   */
  public MutableClock(ZoneId zone) {
    this(Instant.now(), zone);
  }

  /**
   * Initializes a with 1 second {@link #changeAmount} and given parameters.
   * @param instant to use
   * @param zone to use
   */
  public MutableClock(Instant instant, ZoneId zone) {
    this(instant, zone, Duration.ofSeconds(1));
  }

  public MutableClock(Instant instant, ZoneId zone, TemporalAmount changeAmount) {
    this.instant = Objects.requireNonNull(instant,"instant may not be null");
    this.zone = Objects.requireNonNull(zone, "zone may not be null");
    this.changeAmount = Objects.requireNonNull(changeAmount, "changeAmount may not be null");
  }

  /**
   * Initializes a with instant and zone from {@link ZonedDateTime} and 1 second {@link #changeAmount}.
   * @param zonedDateTime to use for instant and zone information
   */
  public MutableClock(ZonedDateTime zonedDateTime) {
    this(zonedDateTime.toInstant(), zonedDateTime.getZone());
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public MutableClock withZone(ZoneId zone) {
    return new MutableClock(instant, zone, changeAmount);
  }

  @Override
  public Instant instant() {
    return instant;
  }

  public void setChangeAmount(TemporalAmount changeAmount) {
    this.changeAmount = Objects.requireNonNull(changeAmount, "changeAmount may not be null");
  }

  public void setInstant(Instant instant) {
    this.instant = Objects.requireNonNull(instant,"instant may not be null");
  }

  /**
   * Sets the internal time based on the result of the modification.
   *
   * @param modification a function to produce a new Instant
   * @return this
   */
  public MutableClock modify(BiFunction<Instant, ZoneId, Instant> modification) {
    instant = modification.apply(instant, zone);
    return this;
  }

  /**
   * Adds the given {@link TemporalAmount} to the internal time.
   *
   * @param amount to add
   * @return this
   */
  public MutableClock plus(TemporalAmount amount) {
    instant = instant.plus(amount);
    return this;
  }

  /**
   * Subtracts the given {@link TemporalAmount} from the internal time.
   *
   * @param amount to subtract
   * @return this
   */
  public MutableClock minus(TemporalAmount amount) {
    instant = instant.minus(amount);
    return this;
  }

  /**
   * Applies the given {@link TemporalAdjuster} to the internal time.
   *
   * @param adjuster the adjuster to use,
   * @return this
   */
  public MutableClock adjust(TemporalAdjuster adjuster) {
    instant = ZonedDateTime.now(this).with(adjuster).toInstant();
    return this;
  }

  /**
   * Adds {@link #changeAmount} to internal time.
   *
   * @return this
   */
  public MutableClock next() {
    return plus(changeAmount);
  }

  /**
   * Subtracts {@link #changeAmount} from internal time.
   *
   * @return this
   */
  public MutableClock previous() {
    return minus(changeAmount);
  }

  @Override
  public String toString() {
    return "MutableClock{" +
      "zone=" + zone +
      ", instant=" + instant +
      ", changeAmount=" + changeAmount +
      '}';
  }
}
