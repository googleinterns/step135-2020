import { CalendarWrapper } from '../lib/wrappers/CalendarWrapper'

describe('displayEventEnd', function() {

  pushOptions({
    initialDate: '2014-06-13',
    timeZone: 'UTC',
    eventTimeFormat: { hour: 'numeric', minute: '2-digit' }
  })

  describeOptions('initialView', {
    'when in month view': 'dayGridMonth',
    'when in week view': 'timeGridWeek'
  }, function() {

    describe('when off', function() {

      pushOptions({
        displayEventEnd: false
      })

      describe('with an all-day event', function() {
        it('displays no time text', function() {
          let calendar = initCalendar({
            events: [ {
              title: 'timed event',
              start: '2014-06-13',
              end: '2014-06-13',
              allDay: true
            } ]
          })
          expectEventTimeText(calendar, '')
        })
      })

      describe('with a timed event with no end time', function() {
        it('displays only the start time text', function() {
          let calendar = initCalendar({
            events: [ {
              title: 'timed event',
              start: '2014-06-13T01:00:00',
              allDay: false
            } ]
          })
          expectEventTimeText(calendar, '1:00 AM')
        })
      })

      describe('with a timed event with an end time', function() {
        it('displays only the start time text', function() {
          let calendar = initCalendar({
            events: [ {
              title: 'timed event',
              start: '2014-06-13T01:00:00',
              end: '2014-06-13T02:00:00',
              allDay: false
            } ]
          })
          expectEventTimeText(calendar, '1:00 AM')
        })
      })
    })

    describe('when on', function() {

      pushOptions({
        displayEventEnd: true
      })

      describe('with an all-day event', function() {
        it('displays no time text', function() {
          let calendar = initCalendar({
            events: [ {
              title: 'timed event',
              start: '2014-06-13',
              end: '2014-06-13',
              allDay: true
            } ]
          })
          expectEventTimeText(calendar, '')
        })
      })

      describe('with a timed event with no end time', function() {
        it('displays only the start time text', function() {
          let calendar = initCalendar({
            events: [ {
              title: 'timed event',
              start: '2014-06-13T01:00:00',
              allDay: false
            } ]
          })
          expectEventTimeText(calendar, '1:00 AM')
        })
      })

      describe('with a timed event given an invalid end time', function() {
        it('displays only the start time text', function() {
          let calendar = initCalendar({
            events: [ {
              title: 'timed event',
              start: '2014-06-13T01:00:00',
              end: '2014-06-13T01:00:00',
              allDay: false
            } ]
          })
          expectEventTimeText(calendar, '1:00 AM')
        })
      })

      describe('with a timed event with an end time', function() {
        it('displays both the start and end time text', function() {
          let calendar = initCalendar({
            events: [ {
              title: 'timed event',
              start: '2014-06-13T01:00:00',
              end: '2014-06-13T02:00:00',
              allDay: false
            } ]
          })
          expectEventTimeText(calendar, '1:00 AM - 2:00 AM')
        })
      })
    })
  })

  function expectEventTimeText(calendar, timeText) {
    let calendarWrapper = new CalendarWrapper(calendar)
    let eventEl = calendarWrapper.getFirstEventEl()
    let eventInfo = calendarWrapper.getEventElInfo(eventEl)

    expect(eventInfo.timeText).toBe(timeText)
  }

})
