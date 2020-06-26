import { CalendarWrapper } from '../lib/wrappers/CalendarWrapper'

describe('addEventSource', function() {
  var eventArray = [
    { id: '0', title: 'event zero', start: '2014-06-24', classNames: 'event-zero' },
    { id: '1', title: 'event one', start: '2014-06-24', classNames: 'event-non-zero event-one' },
    { id: '2', title: 'event two', start: '2014-06-24', classNames: 'event-non-zero event-two' }
  ]

  pushOptions({
    initialDate: '2014-06-24',
    initialView: 'dayGridMonth'
  })


  it('correctly adds an array source', function(done) {
    go(
      function() {
        currentCalendar.addEventSource(eventArray)
      },
      null,
      done
    )
  })

  it('correctly adds a function source', function(done) {
    go(
      function() {
        currentCalendar.addEventSource(function(arg, callback) {
          callback(eventArray)
        })
      },
      null,
      done
    )
  })

  it('correctly adds an extended array source', function(done) {
    go(
      function() {
        currentCalendar.addEventSource({
          classNames: 'arraysource',
          events: eventArray
        })
      },
      function() {
        expect($('.arraysource').length).toEqual(3)
      },
      done
    )
  })

  it('correctly adds an extended func source', function(done) {
    go(
      function() {
        currentCalendar.addEventSource({
          classNames: 'funcsource',
          events: function(arg, callback) {
            callback(eventArray)
          }
        })
      },
      function() {
        expect($('.funcsource').length).toEqual(3)
      },
      done
    )
  })


  function go(addFunc, extraTestFunc, doneFunc) {
    initCalendar()
    addFunc()

    checkAllEvents()
    if (extraTestFunc) {
      extraTestFunc()
    }

    // move the calendar back out of view, then back in (for issue 2191)
    currentCalendar.next()
    currentCalendar.prev()

    // otherwise, prev/next would be cancelled out by doneFunc's calendar destroy
    setTimeout(function() {

      checkAllEvents()
      if (extraTestFunc) {
        extraTestFunc()
      }

      doneFunc()
    }, 0)
  }

  // Checks to make sure all events have been rendered and that the calendar
  // has internal info on all the events.
  function checkAllEvents() {
    expect(currentCalendar.getEvents().length).toEqual(3)

    let calendarWrapper = new CalendarWrapper(currentCalendar)
    expect(calendarWrapper.getEventEls().length).toEqual(3)
  }
})
