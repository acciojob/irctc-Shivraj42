package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        
        Train train= new Train();
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        String route= "";
        for(Station station: trainEntryDto.getStationRoute()){
            route+=station.toString();
        }
        train.setRoute(route);
        Train savedTrain= trainRepository.save(train);
        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

//        Train train= trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
//        String route= train.getRoute();
//        String [] routeStations= route.split(",");
//        int[] bookedSeats= new int[routeStations.length];
//        for(int i=0; i<routeStations.length-1; i++){
//
//            for(Ticket ticket: train.getBookedTickets()){
//
//                if(ticket.getFromStation().toString().equals(routeStations[i])){
//
//                    for(int j=i; j<routeStations.length; j++){
//                        if(routeStations[j].equals(ticket.getToStation().toString())) break;
//                        bookedSeats[j]+=ticket.getPassengersList().size();
//                    }
//
//                }
//
//            }
//        }
//        int maxBookedSeats=0;
//        for(int bookedSeat:bookedSeats){
//            maxBookedSeats= Math.max(maxBookedSeats, bookedSeat);
//        }
//        return train.getNoOfSeats()- maxBookedSeats;

        Train train= trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        String route= train.getRoute();
        String [] routeStations= route.split(",");
        HashMap<String,Integer> map= new HashMap<>();
        for(int i=0; i<routeStations.length; i++){
            map.put(routeStations[i], i);
        }
        int  os= map.get(seatAvailabilityEntryDto.getFromStation().toString());
        int oe= map.get(seatAvailabilityEntryDto.getToStation().toString());
        int totalseatAvailable= train.getNoOfSeats();
        for(Ticket ticket: train.getBookedTickets()){
            int ts= map.get(ticket.getFromStation().toString());
            int te= map.get(ticket.getToStation().toString());
            if(oe>ts && os<te){
                totalseatAvailable-=ticket.getPassengersList().size();
            }
        }
        return totalseatAvailable;


    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train= trainRepository.findById(trainId).get();
        String route= train.getRoute();
        if(!route.contains(station.toString())){
            throw new Exception("Train is not passing from this station");
        }
        int cnt=0;
        for (Ticket ticket:train.getBookedTickets()){
            if(station.equals(ticket.getFromStation())){
                cnt+=ticket.getPassengersList().size();
            }
        }
        return cnt;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Optional<Train> trainOptional= trainRepository.findById(trainId);
        if(!trainOptional.isPresent()){
            return 0;
        }
        Train train= trainOptional.get();
        int maxAge=0;
        for(Ticket ticket: train.getBookedTickets()){
            for(Passenger passenger:ticket.getPassengersList()){
                maxAge=Math.max(maxAge, passenger.getAge());
            }
        }
        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trains= trainRepository.findAll();
        List<Integer> trainIds= new ArrayList<>();
        for(Train train : trains){

            if(train.getRoute().contains(station.toString())){
                String[] stations = train.getRoute().split(",");
                int idx=-1;
                for (int i=0; i<stations.length; i++){
                    if(stations[i].equals(station.toString())) {
                        idx=i;
                        break;
                    }
                }
                LocalTime time= train.getDepartureTime().plusHours(idx);
                if(!time.isAfter(endTime) && !time.isBefore(startTime)){
                    trainIds.add(train.getTrainId());
                }
            }
        }

        return trainIds;
    }

}
