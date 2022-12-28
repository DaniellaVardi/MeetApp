package superapp.logic.concreteServices;

import superapp.boundaries.object.SuperAppObjectBoundary;
import superapp.converters.SuperAppObjectConverter;
import superapp.dal.SuperAppObjectEntityRepository;
import superapp.dal.UserEntityRepository;
import superapp.data.SuperAppObjectEntity;
import superapp.data.UserEntity;
import superapp.logic.MiniappCommandFactory;
import superapp.logic.SplitsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import superapp.logic.SuperAppObjectFactory;
import superapp.util.wrappers.SuperAppObjectIdWrapper;
import superapp.util.wrappers.UserIdWrapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class SplitService implements SplitsService, MiniappCommandFactory, SuperAppObjectFactory {

	private UserEntityRepository userEntityRepository;
	private SuperAppObjectEntityRepository objectRepository;
	private SuperAppObjectConverter converter;

	@Autowired
	public SplitService(UserEntityRepository userEntityRepository, SuperAppObjectEntityRepository objectRepository) {
		super();//
		this.userEntityRepository = userEntityRepository;
		this.objectRepository = objectRepository;
		this.converter = new SuperAppObjectConverter();
	}

	@Override
	public void runCommand(String miniapp, SuperAppObjectIdWrapper targetObject, UserIdWrapper invokedBy, Map<String, Object> attributes, String commandCase) {
		UserEntity user = userEntityRepository.findById(
						new UserEntity.UserPK(invokedBy.getUserId().getSuperapp(), invokedBy.getUserId().getEmail()))
				.get();
		SuperAppObjectEntity group = objectRepository.findById(
						(new SuperAppObjectEntity.SuperAppObjectId(targetObject.getObjectId().getSuperapp(), targetObject.getObjectId().getInternalObjectId())))
				.get();
		switch (commandCase) {
			case "showDebt": {
				this.showDebt(group, user);
				break;
			}
			case "showAllDebts": {
				//TODO add SHOW all Debts
				//	this.showAllDebt(group);
				break;
			}
			case "payDebt": {
				this.payDebt(group, user);
				break;
			}
			default:
				throw new RuntimeException("Command Not Found");
		}
	}

//TODO - Add in Object DeleteObject
//		public void removeTransaction(UserEntity user, SuperAppObjectEntity group, SplitTransaction transaction) {
//        if (!user.equals(transaction.getUserPaid()))
//            throw new RuntimeException("Only the payer can remove the transaction");
//        if (transaction.getGroupDebts().get(user) != transaction.getOriginalPayment())
//            throw new RuntimeException("Cannot close payment , Atleast one user has been paid");
//        SplitGroup split_group = getGroupSplit(group);
//        for (UserEntity trans_user : transaction.getGroupDebts().keySet())
//            transaction.getGroupDebts().put(trans_user, 0.0);
//
//        transaction.setOpen(false);
//        split_group.getExpenses().remove(transaction);
//    }


	@Override
	public double showDebt(SuperAppObjectEntity group, UserEntity user) {
		double allDebt = 0;
		for (SuperAppObjectEntity trans : group.getChildren().stream().filter(t -> t.getType().equals("Transaction")).toList()) {
			Map<UserEntity, Double> AllExpenses = (Map<UserEntity, Double>) this.converter.detailsToMap(trans.getObjectDetails()).get("AllExpenses");
			allDebt += AllExpenses.get(user);

		}
		return allDebt;

		return group.getChildren()
				.stream()
				.filter(t -> t.getType().equals("Transaction"))
				.map(this.converter::toBoundary)
				.map(d->(double)d.getObjectDetails().get("AllExpenses"))
				.reduce(a, b -> a + b);

	}

	@Override
	public void payDebt(SuperAppObjectEntity group, UserEntity user) {//Example : Payed user : 150,Not payed :-50,  Not payed :-50,Not payed :-50
		for (SuperAppObjectEntity trans : group.getChildren().stream().filter(t -> t.getType().equals("Transaction")).toList()) {

			Map<UserEntity, Double> AllExpenses = (Map<UserEntity, Double>) converter.detailsToMap(trans.getObjectDetails()).get("AllExpenses");
			UserEntity paid_user = (UserEntity) converter.detailsToMap(trans.getObjectDetails()).get("paidUser");

			double userDebt = AllExpenses.get(user);
			if (userDebt <= 0)
				throw new RuntimeException("Only Users with debt can pay"); // For Example Trans owner will not able to pay due to his Negative Debt
			else {
				if (!ComputeTransaction(user, converter.toBoundary(trans), userDebt, paid_user, AllExpenses)) { //Example : Payed user : 100,Not payed :0,  Not payed :-50,Not payed :-50
					//TODO removeTransaction
				}
			}
		}

	}


	private boolean ComputeTransaction(UserEntity user, SuperAppObjectBoundary trans, double userDebt, UserEntity paid_user, Map<UserEntity, Double> allExpenses) {
		boolean isOpen = true;
		double paidUserDebts = allExpenses.get(paid_user);
		allExpenses.put(paid_user, paidUserDebts + userDebt);
		allExpenses.put(user, 0.0);
		trans.getObjectDetails().replace("AllExpenses", allExpenses);
		if (allExpenses.get(paid_user) == 0) {
			trans.getObjectDetails().replace("isTransOpen", false);
			isOpen = false;
		}
		this.objectRepository.save(converter.toEntity(trans));
		return isOpen;
	}

	public SuperAppObjectBoundary computeTransactionBalance(SuperAppObjectBoundary trans) { // total_compute_per_group
		HashMap<UserEntity, Double> allExpenses = (HashMap<UserEntity, Double>) trans.getObjectDetails().get("AllExpenses");
		double originalPayment = (Double) trans.getObjectDetails().get("originalPayment");
		for (UserEntity user : allExpenses.keySet()) {
			double balance = allExpenses.get(user);
			double new_balance = balance - originalPayment / allExpenses.keySet().size();
			allExpenses.put(user, new_balance);
		}
		trans.getObjectDetails().replace("allExpenses", allExpenses);
		return trans;
	}


	@Override
	public SuperAppObjectBoundary setObjectDetails(SuperAppObjectBoundary object) {
		if (object.getType() == "Transaction")
			return computeTransactionBalance(object);
		return null;
	}

	@Override
	public SuperAppObjectEntity updateObjectDetails(SuperAppObjectEntity object) {
		return null;
	}
}
